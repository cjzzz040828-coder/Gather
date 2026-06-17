-- 订单退款原因字段 2026-06-17
ALTER TABLE `biz_gb_order`
  ADD COLUMN `refund_reason` VARCHAR(255) DEFAULT NULL COMMENT '退款原因' AFTER `address`;
