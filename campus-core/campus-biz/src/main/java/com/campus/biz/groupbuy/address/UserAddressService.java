package com.campus.biz.groupbuy.address;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 用户收货地址 Service
 */
public interface UserAddressService extends IService<UserAddress> {

    /** 我的地址列表（默认地址置顶） */
    List<UserAddress> listMine(Long userId);

    /** 新增/更新地址（校验归属） */
    Long saveAddress(UserAddress address, Long userId);

    /** 删除地址（校验归属） */
    void removeMine(Long id, Long userId);

    /** 设为默认（清掉同用户其他默认） */
    void setDefault(Long id, Long userId);

    /** 取用户某地址（校验归属，找不到/越权返回 null） */
    UserAddress getMine(Long id, Long userId);
}
