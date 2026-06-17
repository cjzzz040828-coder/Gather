-- 收货地址表 + 订单地址快照字段
-- 2026-06-17 接入下单流程

CREATE TABLE IF NOT EXISTS `biz_user_address` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
  `receiver` VARCHAR(50) NOT NULL COMMENT '收货人',
  `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
  `province` VARCHAR(50) DEFAULT NULL COMMENT '省',
  `city` VARCHAR(50) DEFAULT NULL COMMENT '市',
  `district` VARCHAR(50) DEFAULT NULL COMMENT '区/县',
  `detail` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `is_default` TINYINT DEFAULT 0 COMMENT '是否默认(0否1是)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` BIGINT DEFAULT NULL,
  `update_by` BIGINT DEFAULT NULL,
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除(0否1是)',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收货地址';

-- 订单地址快照（下单时固化，地址后续修改不影响历史订单）
ALTER TABLE `biz_gb_order`
  ADD COLUMN `receiver` VARCHAR(50) DEFAULT NULL COMMENT '收货人快照' AFTER `pay_time`,
  ADD COLUMN `phone` VARCHAR(20) DEFAULT NULL COMMENT '电话快照' AFTER `receiver`,
  ADD COLUMN `address` VARCHAR(400) DEFAULT NULL COMMENT '收货地址快照' AFTER `phone`;
