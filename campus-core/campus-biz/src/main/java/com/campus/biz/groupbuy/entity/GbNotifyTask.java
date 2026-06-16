package com.campus.biz.groupbuy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 拼团本地消息表（替代 MQ，保证成团/退款最终一致）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_gb_notify_task")
public class GbNotifyTask extends BaseEntity {

    /** 业务ID(组队ID) */
    private Long bizId;

    /** 消息类型(team_complete/team_fail) */
    private String messageType;

    /** 消息体(JSON) */
    private String messageBody;

    /** 状态(0-待处理 1-成功 2-失败) */
    private Integer status;

    /** 重试次数 */
    private Integer retryCount;
}
