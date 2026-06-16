package com.campus.biz.groupbuy.trial;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.biz.groupbuy.crowd.CrowdTagService;
import com.campus.biz.groupbuy.dcc.DCCValue;
import com.campus.biz.groupbuy.entity.GbActivity;
import com.campus.biz.groupbuy.entity.GbOrder;
import com.campus.biz.groupbuy.mapper.GbOrderMapper;
import com.campus.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 准入与人群判定守卫：集中承载 DCC 动态开关（降级/切量/黑名单）+ BitMap 人群可见性 + 人群标签判定。
 *
 * <p>抽成独立组件，供试算规则树的"动态开关 / 人群标签过滤"节点与 lockOrder 锁单链路共同复用，
 * 避免开关逻辑在多处重复。DCC 字段在本 bean 上声明，由 DCCValueBeanPostProcessor 注入并支持运行期刷新。</p>
 */
@Component
@RequiredArgsConstructor
public class AccessGuard {

    private final CrowdTagService crowdTagService;
    private final GbOrderMapper orderMapper;

    /** 降级开关：1=暂停拼团（活动维护中）。由 DCC 运行期下发。 */
    @DCCValue("downgradeSwitch:0")
    private String downgradeSwitch;

    /** 用户切量：userId % 100 < cutRange 才放行，用于灰度发布。 */
    @DCCValue("cutRange:100")
    private String cutRange;

    /** 黑名单：逗号分隔的 userId，命中则拒绝参与。 */
    @DCCValue("blackList:")
    private String blackList;

    /** 试算/锁单准入按此顺序圈选的人群标签，命中即作为 userCrowdTag 参与折扣匹配。 */
    private static final List<String> CANDIDATE_CROWD_TAGS = List.of("vip", "newuser");

    /**
     * DCC 动态开关校验：降级 / 黑名单 / 灰度切量。命中拒绝则抛 BusinessException。
     */
    public void checkDynamicSwitch(Long userId) {
        // 1. 降级开关：运营可运行期一键暂停拼团
        if ("1".equals(downgradeSwitch)) {
            throw new BusinessException("活动维护中，暂停拼团，请稍后再试");
        }
        // 2. 黑名单：命中直接拒绝
        if (blackList != null && !blackList.isBlank()) {
            for (String id : blackList.split(",")) {
                if (id.trim().equals(String.valueOf(userId))) {
                    throw new BusinessException("您暂无参与该活动的资格");
                }
            }
        }
        // 3. 用户切量（灰度）：userId % 100 < cutRange 才放行
        int range = parseIntOrDefault(cutRange, 100);
        if (range < 100 && (userId % 100) >= range) {
            throw new BusinessException("活动灰度发布中，您暂未在放量范围内");
        }
    }

    /**
     * BitMap 人群可见性校验：活动配置了可见人群标签时，仅命中该标签的用户可参与。
     */
    public void checkCrowdVisible(GbActivity activity, Long userId) {
        String visibleTag = activity.getVisibleCrowdTag();
        if (visibleTag != null && !visibleTag.isBlank()
                && !crowdTagService.contains(visibleTag, userId)) {
            throw new BusinessException("您不在该活动可参与人群范围内");
        }
    }

    /**
     * 完整准入校验（锁单链路用）：动态开关 + 人群可见性。
     */
    public void checkAccess(GbActivity activity, Long userId) {
        checkDynamicSwitch(userId);
        checkCrowdVisible(activity, userId);
    }

    /**
     * 人群标签判定：优先查 Redis BitMap（运营圈选标签），命中即返回；
     * 都未命中时回落"从未拼单=newuser"的实时判定。
     */
    public String userCrowdTag(Long userId) {
        for (String tag : CANDIDATE_CROWD_TAGS) {
            if (crowdTagService.contains(tag, userId)) {
                return tag;
            }
        }
        Long count = orderMapper.selectCount(new LambdaQueryWrapper<GbOrder>()
                .eq(GbOrder::getUserId, userId));
        return (count == null || count == 0) ? "newuser" : "normal";
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            return value == null || value.isBlank() ? defaultValue : Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
