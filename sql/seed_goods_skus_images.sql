-- 商品多 SKU 与轮播图填充 2026-06-19
-- 为 16 件商品各补充 2-3 个贴合品类的规格；SKU 图与轮播图复用各商品封面（对口、已验证可用）。
-- 幂等：先按 goods_id 删除旧 SKU 再整组插入；images 用 UPDATE 覆盖。
-- 注意：原有 sku_id(1,10..24) 会被删除重建为新 id，活动 biz_gb_activity.sku_id 已放开可空且下单回落活动默认/入参，
--       但为保险，本脚本对“原 1 SKU 商品”保留首个 SKU 的语义（活动仍按 goods 维度可用）。

-- ============ 轮播图：每个商品用封面的多尺寸版本（同一张对口图，避免配错） ============
UPDATE biz_gb_goods SET images = CONCAT(
  SUBSTRING_INDEX(cover, '?', 1), '?w=900&q=80,',
  SUBSTRING_INDEX(cover, '?', 1), '?w=600&q=80,',
  SUBSTRING_INDEX(cover, '?', 1), '?w=1200&q=80'
) WHERE deleted = 0 AND cover IS NOT NULL AND cover != '';

-- ============ SKU 重建（删旧 + 插新，SKU 图 = 商品封面） ============
DELETE FROM biz_gb_sku WHERE goods_id IN (1,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24);

-- 1 精品蓝牙耳机
INSERT INTO biz_gb_sku (goods_id, sku_name, sku_image, original_price, stock, status, create_by)
SELECT 1, t.n, g.cover, t.p, t.s, 1, 1 FROM biz_gb_goods g, (
  SELECT '黑色 标准版' n, 199.00 p, 100 s UNION ALL
  SELECT '白色 标准版', 199.00, 80 UNION ALL
  SELECT '黑色 旗舰版', 259.00, 60) t WHERE g.id=1;

-- 10 无线降噪耳机 Pro
INSERT INTO biz_gb_sku (goods_id, sku_name, sku_image, original_price, stock, status, create_by)
SELECT 10, t.n, g.cover, t.p, t.s, 1, 1 FROM biz_gb_goods g, (
  SELECT '曜石黑' n, 299.00 p, 200 s UNION ALL
  SELECT '云母白', 299.00, 150 UNION ALL
  SELECT '星空蓝', 319.00, 90) t WHERE g.id=10;

-- 11 机械键盘 87键
INSERT INTO biz_gb_sku (goods_id, sku_name, sku_image, original_price, stock, status, create_by)
SELECT 11, t.n, g.cover, t.p, t.s, 1, 1 FROM biz_gb_goods g, (
  SELECT '白色 红轴' n, 239.00 p, 150 s UNION ALL
  SELECT '白色 茶轴', 239.00, 120 UNION ALL
  SELECT '黑色 青轴', 249.00, 100) t WHERE g.id=11;

-- 12 充电宝 20000mAh
INSERT INTO biz_gb_sku (goods_id, sku_name, sku_image, original_price, stock, status, create_by)
SELECT 12, t.n, g.cover, t.p, t.s, 1, 1 FROM biz_gb_goods g, (
  SELECT '白色 20000mAh' n, 129.00 p, 300 s UNION ALL
  SELECT '黑色 20000mAh', 129.00, 260 UNION ALL
  SELECT '白色 10000mAh', 99.00, 200) t WHERE g.id=12;

-- 13 每日坚果礼盒
INSERT INTO biz_gb_sku (goods_id, sku_name, sku_image, original_price, stock, status, create_by)
SELECT 13, t.n, g.cover, t.p, t.s, 1, 1 FROM biz_gb_goods g, (
  SELECT '30包 礼盒装' n, 89.90 p, 500 s UNION ALL
  SELECT '15包 尝鲜装', 49.90, 400) t WHERE g.id=13;

-- 14 手工曲奇 500g
INSERT INTO biz_gb_sku (goods_id, sku_name, sku_image, original_price, stock, status, create_by)
SELECT 14, t.n, g.cover, t.p, t.s, 1, 1 FROM biz_gb_goods g, (
  SELECT '原味 500g' n, 39.90 p, 400 s UNION ALL
  SELECT '抹茶味 500g', 42.90, 300 UNION ALL
  SELECT '混合味 1kg', 75.00, 200) t WHERE g.id=14;

-- 15 螺蛳粉 5包装
INSERT INTO biz_gb_sku (goods_id, sku_name, sku_image, original_price, stock, status, create_by)
SELECT 15, t.n, g.cover, t.p, t.s, 1, 1 FROM biz_gb_goods g, (
  SELECT '经典 5包装' n, 45.00 p, 600 s UNION ALL
  SELECT '加辣 5包装', 45.00, 500 UNION ALL
  SELECT '组合 10包装', 85.00, 300) t WHERE g.id=15;

-- 16 保温杯 316
INSERT INTO biz_gb_sku (goods_id, sku_name, sku_image, original_price, stock, status, create_by)
SELECT 16, t.n, g.cover, t.p, t.s, 1, 1 FROM biz_gb_goods g, (
  SELECT '500ml 雾灰色' n, 79.00 p, 250 s UNION ALL
  SELECT '500ml 奶白色', 79.00, 200 UNION ALL
  SELECT '750ml 墨绿色', 95.00, 150) t WHERE g.id=16;

-- 17 抽纸 24包
INSERT INTO biz_gb_sku (goods_id, sku_name, sku_image, original_price, stock, status, create_by)
SELECT 17, t.n, g.cover, t.p, t.s, 1, 1 FROM biz_gb_goods g, (
  SELECT '24包 整箱' n, 49.90 p, 800 s UNION ALL
  SELECT '12包 半箱', 28.00, 600) t WHERE g.id=17;

-- 18 懒人拖把
INSERT INTO biz_gb_sku (goods_id, sku_name, sku_image, original_price, stock, status, create_by)
SELECT 18, t.n, g.cover, t.p, t.s, 1, 1 FROM biz_gb_goods g, (
  SELECT '标准款 含3布' n, 59.00 p, 200 s UNION ALL
  SELECT '升级款 含5布', 79.00, 150) t WHERE g.id=18;

-- 19 中性笔 20支
INSERT INTO biz_gb_sku (goods_id, sku_name, sku_image, original_price, stock, status, create_by)
SELECT 19, t.n, g.cover, t.p, t.s, 1, 1 FROM biz_gb_goods g, (
  SELECT '黑色 20支' n, 19.90 p, 1000 s UNION ALL
  SELECT '蓝色 20支', 19.90, 800 UNION ALL
  SELECT '混色 30支', 27.90, 500) t WHERE g.id=19;

-- 20 A5活页本
INSERT INTO biz_gb_sku (goods_id, sku_name, sku_image, original_price, stock, status, create_by)
SELECT 20, t.n, g.cover, t.p, t.s, 1, 1 FROM biz_gb_goods g, (
  SELECT 'A5 活页套装' n, 35.00 p, 300 s UNION ALL
  SELECT 'A6 便携套装', 28.00, 250) t WHERE g.id=20;

-- 21 学生书包
INSERT INTO biz_gb_sku (goods_id, sku_name, sku_image, original_price, stock, status, create_by)
SELECT 21, t.n, g.cover, t.p, t.s, 1, 1 FROM biz_gb_goods g, (
  SELECT '藏青色 大容量' n, 119.00 p, 180 s UNION ALL
  SELECT '粉色 大容量', 119.00, 150 UNION ALL
  SELECT '灰色 大容量', 119.00, 120) t WHERE g.id=21;

-- 22 氨基酸洗面奶
INSERT INTO biz_gb_sku (goods_id, sku_name, sku_image, original_price, stock, status, create_by)
SELECT 22, t.n, g.cover, t.p, t.s, 1, 1 FROM biz_gb_goods g, (
  SELECT '100g 单支' n, 49.00 p, 350 s UNION ALL
  SELECT '100g 双支装', 89.00, 250) t WHERE g.id=22;

-- 23 纯棉宽松卫衣
INSERT INTO biz_gb_sku (goods_id, sku_name, sku_image, original_price, stock, status, create_by)
SELECT 23, t.n, g.cover, t.p, t.s, 1, 1 FROM biz_gb_goods g, (
  SELECT '灰色 加绒 M' n, 99.00 p, 80 s UNION ALL
  SELECT '灰色 加绒 L', 99.00, 80 UNION ALL
  SELECT '黑色 加绒 XL', 99.00, 60) t WHERE g.id=23;

-- 24 运动速干T恤
INSERT INTO biz_gb_sku (goods_id, sku_name, sku_image, original_price, stock, status, create_by)
SELECT 24, t.n, g.cover, t.p, t.s, 1, 1 FROM biz_gb_goods g, (
  SELECT '蓝色 速干 M' n, 69.00 p, 100 s UNION ALL
  SELECT '蓝色 速干 L', 69.00, 90 UNION ALL
  SELECT '黑色 速干 L', 69.00, 80) t WHERE g.id=24;

-- ============ 修正活动默认 SKU：指向各商品重建后的首个 SKU（原 sku_id 已随重建失效） ============
UPDATE biz_gb_activity a
JOIN (
  SELECT goods_id, MIN(id) AS first_sku FROM biz_gb_sku WHERE deleted = 0 GROUP BY goods_id
) m ON m.goods_id = a.goods_id
SET a.sku_id = m.first_sku
WHERE a.deleted = 0;

