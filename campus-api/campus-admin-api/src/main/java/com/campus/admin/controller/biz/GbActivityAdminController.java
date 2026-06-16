package com.campus.admin.controller.biz;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.campus.biz.groupbuy.entity.GbActivity;
import com.campus.biz.groupbuy.entity.GbDiscount;
import com.campus.biz.groupbuy.service.GbActivityService;
import com.campus.common.result.PageResult;
import com.campus.common.result.Result;
import com.campus.system.annotation.Log;
import com.campus.system.annotation.Log.BusinessType;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 拼团活动 - 后台管理
 */
@RestController
@RequestMapping("/biz/gbActivity")
@RequiredArgsConstructor
public class GbActivityAdminController {

    private final GbActivityService gbActivityService;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    @SaCheckPermission("biz:gbActivity:list")
    public Result<PageResult<GbActivity>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long goodsId,
            @RequestParam(required = false) Integer status) {
        return Result.ok(PageResult.of(gbActivityService.adminPage(page, pageSize, goodsId, status)));
    }

    /**
     * 详情（含折扣列表）
     */
    @GetMapping("/{id}")
    @SaCheckPermission("biz:gbActivity:list")
    public Result<ActivityDetailVO> getInfo(@PathVariable Long id) {
        ActivityDetailVO vo = new ActivityDetailVO();
        vo.setActivity(gbActivityService.getById(id));
        vo.setDiscounts(gbActivityService.listDiscounts(id));
        return Result.ok(vo);
    }

    /**
     * 新增（活动 + 折扣）
     */
    @PostMapping
    @SaCheckPermission("biz:gbActivity:add")
    @Log(title = "拼团活动", businessType = BusinessType.INSERT)
    public Result<Long> add(@RequestBody ActivitySaveRequest request) {
        return Result.ok(gbActivityService.saveActivity(request.getActivity(), request.getDiscounts()));
    }

    /**
     * 修改（活动 + 折扣）
     */
    @PutMapping
    @SaCheckPermission("biz:gbActivity:edit")
    @Log(title = "拼团活动", businessType = BusinessType.UPDATE)
    public Result<Long> edit(@RequestBody ActivitySaveRequest request) {
        return Result.ok(gbActivityService.saveActivity(request.getActivity(), request.getDiscounts()));
    }

    /**
     * 更新状态
     */
    @PutMapping("/status")
    @SaCheckPermission("biz:gbActivity:edit")
    @Log(title = "拼团活动", businessType = BusinessType.UPDATE)
    public Result<Void> changeStatus(@RequestBody StatusRequest request) {
        gbActivityService.changeStatus(request.getId(), request.getStatus());
        return Result.ok();
    }

    /**
     * 删除（连带折扣）
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("biz:gbActivity:remove")
    @Log(title = "拼团活动", businessType = BusinessType.DELETE)
    public Result<Void> remove(@PathVariable Long id) {
        gbActivityService.removeActivity(id);
        return Result.ok();
    }

    @Data
    public static class ActivitySaveRequest {
        private GbActivity activity;
        private List<GbDiscount> discounts;
    }

    @Data
    public static class ActivityDetailVO {
        private GbActivity activity;
        private List<GbDiscount> discounts;
    }

    @Data
    public static class StatusRequest {
        private Long id;
        private Integer status;
    }
}
