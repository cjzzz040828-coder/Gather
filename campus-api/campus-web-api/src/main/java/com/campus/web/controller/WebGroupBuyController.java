package com.campus.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.campus.biz.groupbuy.dto.LockOrderDTO;
import com.campus.biz.groupbuy.dto.LockOrderResultDTO;
import com.campus.biz.groupbuy.dto.TrialResultDTO;
import com.campus.biz.groupbuy.entity.GbActivity;
import com.campus.biz.groupbuy.entity.GbDiscount;
import com.campus.biz.groupbuy.entity.GbGoods;
import com.campus.biz.groupbuy.entity.GbOrder;
import com.campus.biz.groupbuy.entity.GbSku;
import com.campus.biz.groupbuy.entity.GbTeam;
import com.campus.biz.groupbuy.service.*;
import com.campus.common.result.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PC 用户商城 - 拼团交易控制器
 *
 * <p>商品列表/详情免登录（见 SaTokenConfig notMatch）；试算/锁单/支付回调/我的拼团需登录。</p>
 */
@RestController
@RequestMapping("/web/groupbuy")
@RequiredArgsConstructor
public class WebGroupBuyController {

    private final GbGoodsService gbGoodsService;
    private final GbActivityService gbActivityService;
    private final GbTeamService gbTeamService;
    private final GbOrderService gbOrderService;
    private final GbTradeService gbTradeService;

    /**
     * 上架商品列表（公开）
     */
    @GetMapping("/goods")
    public Result<List<GbGoods>> goodsList(@RequestParam(required = false) String title) {
        return Result.ok(gbGoodsService.adminPage(1, 100, title, 1).getRecords());
    }

    /**
     * 上架商品分页（公开，含最低价、分类筛选、价格排序）
     */
    @GetMapping("/goods/page")
    public Result<GoodsPageVO> goodsPage(@RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "12") Integer pageSize,
                                         @RequestParam(required = false) String title,
                                         @RequestParam(required = false) String category,
                                         @RequestParam(required = false) String sort) {
        var p = gbGoodsService.webPage(page, pageSize, title, category, sort);
        GoodsPageVO vo = new GoodsPageVO();
        vo.setList(p.getRecords());
        vo.setTotal(p.getTotal());
        return Result.ok(vo);
    }

    /**
     * 上架商品分类列表（公开）
     */
    @GetMapping("/goods/categories")
    public Result<List<String>> goodsCategories() {
        return Result.ok(gbGoodsService.listCategories());
    }

    /**
     * 商品详情（含 SKU，公开）
     */
    @GetMapping("/goods/{id}")
    public Result<GoodsDetailVO> goodsDetail(@PathVariable Long id) {
        GoodsDetailVO vo = new GoodsDetailVO();
        vo.setGoods(gbGoodsService.getById(id));
        vo.setSkus(gbGoodsService.listSkus(id));
        return Result.ok(vo);
    }

    /**
     * 某商品下进行中的活动列表（公开，供商品详情页"去拼团"定位活动）
     */
    @GetMapping("/goods/{id}/activities")
    public Result<List<GbActivity>> goodsActivities(@PathVariable Long id) {
        return Result.ok(gbActivityService.listRunningByGoodsId(id));
    }

    /**
     * 活动详情（含折扣 + 可加入的拼团组队，公开）
     */
    @GetMapping("/activity/{id}")
    public Result<ActivityDetailVO> activityDetail(@PathVariable Long id) {
        ActivityDetailVO vo = new ActivityDetailVO();
        vo.setActivity(gbActivityService.getById(id));
        vo.setDiscounts(gbActivityService.listDiscounts(id));
        vo.setJoinableTeams(gbTeamService.listJoinable(id));
        return Result.ok(vo);
    }

    /**
     * 试算（需登录）
     */
    @GetMapping("/trial")
    public Result<TrialResultDTO> trial(@RequestParam Long activityId) {
        return Result.ok(gbTradeService.trial(activityId, StpUtil.getLoginIdAsLong()));
    }

    /**
     * 发起/参与拼团（锁单，需登录）
     */
    @PostMapping("/lock")
    public Result<LockOrderResultDTO> lockOrder(@RequestBody LockOrderDTO dto) {
        return Result.ok(gbTradeService.lockOrder(dto, StpUtil.getLoginIdAsLong()));
    }

    /**
     * 模拟支付完成回调（需登录）
     */
    @PostMapping("/pay-callback")
    public Result<Void> payCallback(@RequestBody PayCallbackRequest request) {
        gbTradeService.payCallback(request.getOutTradeNo());
        return Result.ok();
    }

    /**
     * 我的拼单（需登录）
     */
    @GetMapping("/my-orders")
    public Result<List<GbOrder>> myOrders() {
        return Result.ok(gbOrderService.listMine(StpUtil.getLoginIdAsLong()));
    }

    /**
     * 组队进度（需登录）
     */
    @GetMapping("/team/{id}")
    public Result<GbTeam> teamProgress(@PathVariable Long id) {
        return Result.ok(gbTeamService.getById(id));
    }

    @Data
    public static class GoodsDetailVO {
        private GbGoods goods;
        private List<GbSku> skus;
    }

    @Data
    public static class GoodsPageVO {
        private List<GbGoods> list;
        private Long total;
    }

    @Data
    public static class ActivityDetailVO {
        private GbActivity activity;
        private List<GbDiscount> discounts;
        private List<GbTeam> joinableTeams;
    }

    @Data
    public static class PayCallbackRequest {
        private String outTradeNo;
    }
}
