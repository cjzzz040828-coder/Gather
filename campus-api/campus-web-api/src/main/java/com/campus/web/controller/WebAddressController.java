package com.campus.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.campus.biz.groupbuy.address.UserAddress;
import com.campus.biz.groupbuy.address.UserAddressService;
import com.campus.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PC 用户 - 收货地址控制器（均需登录，按当前用户隔离）
 */
@RestController
@RequestMapping("/web/address")
@RequiredArgsConstructor
public class WebAddressController {

    private final UserAddressService addressService;

    /** 我的地址列表 */
    @GetMapping("/list")
    public Result<List<UserAddress>> list() {
        return Result.ok(addressService.listMine(StpUtil.getLoginIdAsLong()));
    }

    /** 新增地址 */
    @PostMapping
    public Result<Long> add(@RequestBody UserAddress address) {
        address.setId(null);
        return Result.ok(addressService.saveAddress(address, StpUtil.getLoginIdAsLong()));
    }

    /** 更新地址 */
    @PutMapping
    public Result<Long> update(@RequestBody UserAddress address) {
        return Result.ok(addressService.saveAddress(address, StpUtil.getLoginIdAsLong()));
    }

    /** 删除地址 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        addressService.removeMine(id, StpUtil.getLoginIdAsLong());
        return Result.ok();
    }

    /** 设为默认 */
    @PutMapping("/default/{id}")
    public Result<Void> setDefault(@PathVariable Long id) {
        addressService.setDefault(id, StpUtil.getLoginIdAsLong());
        return Result.ok();
    }
}
