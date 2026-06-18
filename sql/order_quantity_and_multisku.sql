-- 拼单数量 + 一活动多 SKU 2026-06-19
-- 订单记录购买数量（一人买 N 件）；金额列继续存总价
ALTER TABLE `biz_gb_order`
  ADD COLUMN `quantity` INT NOT NULL DEFAULT 1 COMMENT '购买数量' AFTER `sku_id`;

-- 活动 sku_id 由 NOT NULL 放开为可空，降级为「默认 SKU」；下单时入参 skuId 优先，为空回落此默认
ALTER TABLE `biz_gb_activity`
  MODIFY COLUMN `sku_id` BIGINT NULL DEFAULT NULL COMMENT '默认SKU(可空，下单可指定其它同商品SKU)';
