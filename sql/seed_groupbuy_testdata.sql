-- 拼团测试数据：15 件商品 + SKU + 进行中活动
-- id 从 10 起，避开现有 goods=1/sku=1/activity=1
SET @now = NOW();
SET @end = DATE_ADD(NOW(), INTERVAL 30 DAY);

-- 商品 (id, title, cover, detail, category, status, create_by)
INSERT INTO `biz_gb_goods` (id,title,cover,detail,category,status,create_by,create_time,update_time,deleted) VALUES
(10,'【拼团】无线降噪耳机 Pro','https://picsum.photos/seed/gb10/400/400','<p>主动降噪，30小时续航，拼团立省。</p>','数码',1,1,@now,@now,0),
(11,'【拼团】机械键盘 87键','https://picsum.photos/seed/gb11/400/400','<p>青轴手感，RGB背光，电竞首选。</p>','数码',1,1,@now,@now,0),
(12,'【拼团】便携充电宝 20000mAh','https://picsum.photos/seed/gb12/400/400','<p>大容量快充，双口输出。</p>','数码',1,1,@now,@now,0),
(13,'【拼团】每日坚果礼盒 30包','https://picsum.photos/seed/gb13/400/400','<p>七种坚果搭配，独立小包装。</p>','零食',1,1,@now,@now,0),
(14,'【拼团】手工曲奇饼干 500g','https://picsum.photos/seed/gb14/400/400','<p>黄油香浓，酥脆可口。</p>','零食',1,1,@now,@now,0),
(15,'【拼团】网红螺蛳粉 5包装','https://picsum.photos/seed/gb15/400/400','<p>正宗柳州风味，料包充足。</p>','零食',1,1,@now,@now,0),
(16,'【拼团】保温杯 316不锈钢','https://picsum.photos/seed/gb16/400/400','<p>24小时保温，大容量便携。</p>','生活',1,1,@now,@now,0),
(17,'【拼团】抽纸 24包整箱','https://picsum.photos/seed/gb17/400/400','<p>原木浆，柔韧亲肤，整箱更划算。</p>','生活',1,1,@now,@now,0),
(18,'【拼团】懒人拖把 免手洗','https://picsum.photos/seed/gb18/400/400','<p>旋转脱水，免手洗，省力清洁。</p>','生活',1,1,@now,@now,0),
(19,'【拼团】中性笔 0.5mm 20支','https://picsum.photos/seed/gb19/400/400','<p>速干顺滑，考试办公必备。</p>','文具',1,1,@now,@now,0),
(20,'【拼团】A5活页笔记本套装','https://picsum.photos/seed/gb20/400/400','<p>可拆卸活页，附赠分隔页。</p>','文具',1,1,@now,@now,0),
(21,'【拼团】学生书包 大容量','https://picsum.photos/seed/gb21/400/400','<p>护脊减负，防泼水面料。</p>','文具',1,1,@now,@now,0),
(22,'【拼团】氨基酸洗面奶 100g','https://picsum.photos/seed/gb22/400/400','<p>温和清洁，不紧绷。</p>','美妆',1,1,@now,@now,0),
(23,'【拼团】纯棉宽松卫衣','https://picsum.photos/seed/gb23/400/400','<p>加绒保暖，男女同款。</p>','服饰',1,1,@now,@now,0),
(24,'【拼团】运动速干T恤','https://picsum.photos/seed/gb24/400/400','<p>透气速干，弹力面料。</p>','服饰',1,1,@now,@now,0);

-- SKU (id, goods_id, sku_name, original_price, stock, status)
INSERT INTO `biz_gb_sku` (id,goods_id,sku_name,original_price,stock,status,create_by,create_time,update_time,deleted) VALUES
(10,10,'黑色 标准版',299.00,200,1,1,@now,@now,0),
(11,11,'白色 87键',239.00,150,1,1,@now,@now,0),
(12,12,'白色 20000mAh',129.00,300,1,1,@now,@now,0),
(13,13,'30包 礼盒装',89.90,500,1,1,@now,@now,0),
(14,14,'原味 500g',39.90,400,1,1,@now,@now,0),
(15,15,'5包 组合装',45.00,600,1,1,@now,@now,0),
(16,16,'500ml 雾灰色',79.00,250,1,1,@now,@now,0),
(17,17,'24包 整箱',49.90,800,1,1,@now,@now,0),
(18,18,'标准款 含3布',59.00,200,1,1,@now,@now,0),
(19,19,'黑色 20支',19.90,1000,1,1,@now,@now,0),
(20,20,'A5 活页套装',35.00,300,1,1,@now,@now,0),
(21,21,'藏青色 大容量',119.00,180,1,1,@now,@now,0),
(22,22,'100g 单支',49.00,350,1,1,@now,@now,0),
(23,23,'灰色 加绒 M',99.00,220,1,1,@now,@now,0),
(24,24,'蓝色 速干 L',69.00,260,1,1,@now,@now,0);

-- 活动 (id, goods_id, sku_id, activity_name, target_count, valid_start, valid_end, time_limit_minutes, status)
INSERT INTO `biz_gb_activity` (id,goods_id,sku_id,activity_name,target_count,valid_start,valid_end,time_limit_minutes,status,create_by,create_time,update_time,deleted) VALUES
(10,10,10,'降噪耳机 2人拼团',2,@now,@end,1440,1,1,@now,@now,0),
(11,11,11,'机械键盘 3人拼团',3,@now,@end,1440,1,1,@now,@now,0),
(12,12,12,'充电宝 2人拼团',2,@now,@end,1440,1,1,@now,@now,0),
(13,13,13,'坚果礼盒 3人拼团',3,@now,@end,1440,1,1,@now,@now,0),
(14,14,14,'手工曲奇 2人拼团',2,@now,@end,1440,1,1,@now,@now,0),
(15,15,15,'螺蛳粉 3人拼团',3,@now,@end,1440,1,1,@now,@now,0),
(16,16,16,'保温杯 2人拼团',2,@now,@end,1440,1,1,@now,@now,0),
(17,17,17,'抽纸整箱 3人拼团',3,@now,@end,1440,1,1,@now,@now,0),
(18,18,18,'懒人拖把 2人拼团',2,@now,@end,1440,1,1,@now,@now,0),
(19,19,19,'中性笔 3人拼团',3,@now,@end,1440,1,1,@now,@now,0),
(20,20,20,'活页本 2人拼团',2,@now,@end,1440,1,1,@now,@now,0),
(21,21,21,'学生书包 2人拼团',2,@now,@end,1440,1,1,@now,@now,0),
(22,22,22,'洗面奶 3人拼团',3,@now,@end,1440,1,1,@now,@now,0),
(23,23,23,'卫衣 2人拼团',2,@now,@end,1440,1,1,@now,@now,0),
(24,24,24,'速干T恤 3人拼团',3,@now,@end,1440,1,1,@now,@now,0);

-- 优惠规则：每个活动一条直减(discount_type=1 直减, discount_value=减免金额)
INSERT INTO `biz_gb_discount` (id,activity_id,discount_type,discount_value,crowd_tag,priority,create_time,update_time,deleted) VALUES
(10,10,1,50.00,NULL,1,@now,@now,0),
(11,11,1,40.00,NULL,1,@now,@now,0),
(12,12,1,20.00,NULL,1,@now,@now,0),
(13,13,1,15.00,NULL,1,@now,@now,0),
(14,14,1,8.00,NULL,1,@now,@now,0),
(15,15,1,10.00,NULL,1,@now,@now,0),
(16,16,1,15.00,NULL,1,@now,@now,0),
(17,17,1,10.00,NULL,1,@now,@now,0),
(18,18,1,12.00,NULL,1,@now,@now,0),
(19,19,1,5.00,NULL,1,@now,@now,0),
(20,20,1,8.00,NULL,1,@now,@now,0),
(21,21,1,25.00,NULL,1,@now,@now,0),
(22,22,1,12.00,NULL,1,@now,@now,0),
(23,23,1,20.00,NULL,1,@now,@now,0),
(24,24,1,15.00,NULL,1,@now,@now,0);
