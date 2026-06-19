-- 商品管理体系扩展：商品 5 字段 + SKU 规格图 2026-06-19
ALTER TABLE `biz_gb_goods`
  ADD COLUMN `subtitle`     VARCHAR(255)  NULL DEFAULT NULL COMMENT '副标题'           AFTER `title`,
  ADD COLUMN `market_price` DECIMAL(10,2) NULL DEFAULT NULL COMMENT '划线价/市场价'    AFTER `detail`,
  ADD COLUMN `sales`        INT       NOT NULL DEFAULT 0    COMMENT '销量'             AFTER `market_price`,
  ADD COLUMN `sort`         INT       NOT NULL DEFAULT 0    COMMENT '排序权重(大在前)'  AFTER `sales`,
  ADD COLUMN `publish_time` DATETIME      NULL DEFAULT NULL COMMENT '上架时间'          AFTER `sort`;

ALTER TABLE `biz_gb_sku`
  ADD COLUMN `sku_image` VARCHAR(500) NULL DEFAULT NULL COMMENT '规格图片URL' AFTER `sku_name`;

ALTER TABLE `biz_gb_goods` ADD INDEX `idx_status_sort`(`status`, `sort`, `id`);
