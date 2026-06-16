package com.campus.biz.groupbuy.dto;

import lombok.Data;

/**
 * 锁单请求
 */
@Data
public class LockOrderDTO {

    /** 活动ID */
    private Long activityId;

    /** 加入的组队ID（为空则新建团并成为团长） */
    private Long teamId;
}
