package com.campus.admin.controller.biz;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.campus.biz.groupbuy.entity.GbGoods;
import com.campus.biz.groupbuy.entity.GbSku;
import com.campus.biz.groupbuy.service.GbGoodsService;
import com.campus.common.result.PageResult;
import com.campus.common.result.Result;
import com.campus.system.annotation.Log;
import com.campus.system.annotation.Log.BusinessType;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 拼团商品 - 后台管理
 */
@RestController
@RequestMapping("/biz/gbGoods")
@RequiredArgsConstructor
public class GbGoodsAdminController {

    private final GbGoodsService gbGoodsService;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    @SaCheckPermission("biz:gbGoods:list")
    public Result<PageResult<GbGoods>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status) {
        return Result.ok(PageResult.of(gbGoodsService.adminPage(page, pageSize, title, status)));
    }

    /**
     * 详情（含 SKU 列表）
     */
    @GetMapping("/{id}")
    @SaCheckPermission("biz:gbGoods:list")
    public Result<GoodsDetailVO> getInfo(@PathVariable Long id) {
        GoodsDetailVO vo = new GoodsDetailVO();
        vo.setGoods(gbGoodsService.getById(id));
        vo.setSkus(gbGoodsService.listSkus(id));
        return Result.ok(vo);
    }

    /**
     * 新增（商品 + SKU）
     */
    @PostMapping
    @SaCheckPermission("biz:gbGoods:add")
    @Log(title = "拼团商品", businessType = BusinessType.INSERT)
    public Result<Long> add(@RequestBody GoodsSaveRequest request) {
        return Result.ok(gbGoodsService.saveGoods(request.getGoods(), request.getSkus()));
    }

    /**
     * 修改（商品 + SKU）
     */
    @PutMapping
    @SaCheckPermission("biz:gbGoods:edit")
    @Log(title = "拼团商品", businessType = BusinessType.UPDATE)
    public Result<Long> edit(@RequestBody GoodsSaveRequest request) {
        return Result.ok(gbGoodsService.saveGoods(request.getGoods(), request.getSkus()));
    }

    /**
     * 上架 / 下架
     */
    @PutMapping("/status")
    @SaCheckPermission("biz:gbGoods:status")
    @Log(title = "拼团商品", businessType = BusinessType.UPDATE)
    public Result<Void> changeStatus(@RequestBody StatusRequest request) {
        gbGoodsService.changeStatus(request.getId(), request.getStatus());
        return Result.ok();
    }

    /**
     * 删除（连带 SKU）
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("biz:gbGoods:remove")
    @Log(title = "拼团商品", businessType = BusinessType.DELETE)
    public Result<Void> remove(@PathVariable Long id) {
        gbGoodsService.removeGoods(id);
        return Result.ok();
    }

    @Data
    public static class GoodsSaveRequest {
        private GbGoods goods;
        private List<GbSku> skus;
    }

    @Data
    public static class GoodsDetailVO {
        private GbGoods goods;
        private List<GbSku> skus;
    }

    @Data
    public static class StatusRequest {
        private Long id;
        private Integer status;
    }
}
