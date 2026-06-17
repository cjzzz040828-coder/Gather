package com.campus.biz.groupbuy.address;

import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户收货地址
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_user_address")
public class UserAddress extends BaseEntity {

    /** 所属用户ID */
    private Long userId;

    /** 收货人 */
    private String receiver;

    /** 联系电话 */
    private String phone;

    /** 省 */
    private String province;

    /** 市 */
    private String city;

    /** 区/县 */
    private String district;

    /** 详细地址 */
    private String detail;

    /** 是否默认(0否1是) */
    private Integer isDefault;
}
