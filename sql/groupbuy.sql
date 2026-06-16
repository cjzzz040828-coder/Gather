-- ======================================================================
-- 拼团交易营销模块 - 业务表与菜单种子（借鉴小傅哥 group-buy-market）
-- 依赖：campus-system.sql（需先导入基础 RBAC 表）
-- 库：campus-system
-- 表前缀：biz_gb_   （gb = group buy）
-- ======================================================================
USE `campus-system`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ======================================================================
-- 1. 拼团商品
-- ======================================================================
DROP TABLE IF EXISTS `biz_gb_goods`;
CREATE TABLE `biz_gb_goods` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(200) NOT NULL COMMENT '商品标题',
  `cover` varchar(500) NULL DEFAULT NULL COMMENT '封面图URL',
  `images` varchar(2000) NULL DEFAULT NULL COMMENT '轮播图URL(逗号分隔)',
  `detail` longtext NULL COMMENT '图文详情(富文本)',
  `category` varchar(50) NULL DEFAULT NULL COMMENT '商品分类',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态(0-下架 1-上架)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '删除标识(0-未删除 1-已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '拼团商品表' ROW_FORMAT = DYNAMIC;

-- ======================================================================
-- 2. 商品 SKU（规格）
-- ======================================================================
DROP TABLE IF EXISTS `biz_gb_sku`;
CREATE TABLE `biz_gb_sku` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `sku_name` varchar(200) NOT NULL COMMENT '规格名称',
  `original_price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '原价',
  `stock` int NOT NULL DEFAULT 0 COMMENT '库存',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态(0-停用 1-启用)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '删除标识(0-未删除 1-已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_goods_id`(`goods_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '拼团商品SKU表' ROW_FORMAT = DYNAMIC;

-- ======================================================================
-- 3. 拼团活动
-- ======================================================================
DROP TABLE IF EXISTS `biz_gb_activity`;
CREATE TABLE `biz_gb_activity` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `activity_name` varchar(200) NOT NULL COMMENT '活动名称',
  `target_count` int NOT NULL DEFAULT 2 COMMENT '成团人数',
  `valid_start` datetime NOT NULL COMMENT '活动开始时间',
  `valid_end` datetime NOT NULL COMMENT '活动结束时间',
  `time_limit_minutes` int NOT NULL DEFAULT 1440 COMMENT '单团时限(分钟)',
  `visible_crowd_tag` varchar(64) NULL DEFAULT NULL COMMENT '可见人群标签(空=全部可见;非空时由Redis BitMap判定可见可参与)',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态(0-未开始 1-进行中 2-已结束)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '删除标识(0-未删除 1-已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_goods_id`(`goods_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '拼团活动表' ROW_FORMAT = DYNAMIC;

-- ======================================================================
-- 4. 拼团折扣（与活动 1:N，借鉴小傅哥折扣/活动拆分）
-- ======================================================================
DROP TABLE IF EXISTS `biz_gb_discount`;
CREATE TABLE `biz_gb_discount` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `discount_type` tinyint NOT NULL COMMENT '折扣类型(1-直减 2-折扣 3-N元购)',
  `discount_value` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '折扣值(直减额/折扣率/N元价)',
  `crowd_tag` varchar(50) NULL DEFAULT NULL COMMENT '人群标签(空=全部人群)',
  `priority` int NOT NULL DEFAULT 0 COMMENT '优先级(越大越优先)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '删除标识(0-未删除 1-已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_activity_id`(`activity_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '拼团折扣表' ROW_FORMAT = DYNAMIC;

-- ======================================================================
-- 5. 拼团组队（进度表）
-- ======================================================================
DROP TABLE IF EXISTS `biz_gb_team`;
CREATE TABLE `biz_gb_team` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `leader_id` bigint NOT NULL COMMENT '团长用户ID',
  `target_count` int NOT NULL COMMENT '成团目标人数',
  `lock_count` int NOT NULL DEFAULT 0 COMMENT '已锁单人数',
  `complete_count` int NOT NULL DEFAULT 0 COMMENT '已支付人数',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态(0-拼团中 1-成功 2-失败)',
  `valid_end_time` datetime NOT NULL COMMENT '本团截止时间(建团时刻+单团时限)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '删除标识(0-未删除 1-已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_activity_id`(`activity_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '拼团组队表' ROW_FORMAT = DYNAMIC;

-- ======================================================================
-- 6. 拼单记录（out_trade_no 唯一索引防重）
-- ======================================================================
DROP TABLE IF EXISTS `biz_gb_order`;
CREATE TABLE `biz_gb_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `team_id` bigint NOT NULL COMMENT '组队ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `user_id` bigint NOT NULL COMMENT '拼单用户ID',
  `out_trade_no` varchar(64) NOT NULL COMMENT '外部交易单号',
  `original_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '原始金额',
  `deduction_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
  `pay_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '实付金额',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态(0-锁定 1-已支付 2-成团 3-退款)',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '删除标识(0-未删除 1-已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_out_trade_no`(`out_trade_no` ASC) USING BTREE,
  INDEX `idx_team_id`(`team_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '拼单记录表' ROW_FORMAT = DYNAMIC;

-- ======================================================================
-- 7. 本地消息表（替代 MQ，保证成团/退款最终一致）
-- ======================================================================
DROP TABLE IF EXISTS `biz_gb_notify_task`;
CREATE TABLE `biz_gb_notify_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `biz_id` bigint NOT NULL COMMENT '业务ID(组队ID)',
  `message_type` varchar(50) NOT NULL COMMENT '消息类型(team_complete/team_fail)',
  `message_body` text NULL COMMENT '消息体(JSON)',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态(0-待处理 1-成功 2-失败)',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '重试次数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '删除标识(0-未删除 1-已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_biz_id`(`biz_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '拼团本地消息表' ROW_FORMAT = DYNAMIC;

-- ======================================================================
-- 种子数据：1 商品 + 1 SKU + 1 进行中活动 + 2 折扣（便于直接跑通）
-- ======================================================================
INSERT INTO `biz_gb_goods` (`id`, `title`, `cover`, `detail`, `category`, `status`, `create_by`)
VALUES (1, '【拼团】精品蓝牙耳机', NULL, '<p>降噪蓝牙耳机，拼团更优惠。</p>', '数码', 1, 1);

INSERT INTO `biz_gb_sku` (`id`, `goods_id`, `sku_name`, `original_price`, `stock`, `status`, `create_by`)
VALUES (1, 1, '黑色 标准版', 199.00, 100, 1, 1);

-- 活动：即刻生效，结束于 30 天后，单团时限 1 天，2 人成团
INSERT INTO `biz_gb_activity` (`id`, `goods_id`, `sku_id`, `activity_name`, `target_count`, `valid_start`, `valid_end`, `time_limit_minutes`, `status`, `create_by`)
VALUES (1, 1, 1, '蓝牙耳机 2 人拼团', 2, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1440, 1, 1);

-- 折扣：全人群直减 50；标记 newuser 人群 99 元购（更高优先级）
INSERT INTO `biz_gb_discount` (`activity_id`, `discount_type`, `discount_value`, `crowd_tag`, `priority`, `create_by`)
VALUES (1, 1, 50.00, NULL, 1, 1),
       (1, 3, 99.00, 'newuser', 10, 1);

-- ======================================================================
-- 后台菜单与权限（menu ID 从 500 起，避开现有最大值 432；role_menu 从 9100 起，避开 9032）
--   顶级目录 500「拼团营销」/groupbuy
-- ======================================================================
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission`, `icon`, `sort`, `visible`, `status`, `is_frame`, `create_by`) VALUES
  (500, 0,   '拼团营销', 1, '/groupbuy',          NULL,                          NULL,                  'BagOutline',         20, 1, 1, 0, 1),

  -- 商品管理
  (510, 500, '商品管理', 2, '/groupbuy/goods',    '/groupbuy/goods/index',       'biz:gbGoods:list',    'CubeOutline',        1,  1, 1, 0, 1),
  (511, 510, '新增商品', 3, NULL,                  NULL,                          'biz:gbGoods:add',     NULL,                 1,  1, 1, 0, 1),
  (512, 510, '编辑商品', 3, NULL,                  NULL,                          'biz:gbGoods:edit',    NULL,                 2,  1, 1, 0, 1),
  (513, 510, '删除商品', 3, NULL,                  NULL,                          'biz:gbGoods:remove',  NULL,                 3,  1, 1, 0, 1),
  (514, 510, '上架/下架', 3, NULL,                 NULL,                          'biz:gbGoods:status',  NULL,                 4,  1, 1, 0, 1),

  -- 活动管理
  (520, 500, '活动管理', 2, '/groupbuy/activity', '/groupbuy/activity/index',    'biz:gbActivity:list', 'FlashOutline',       2,  1, 1, 0, 1),
  (521, 520, '新增活动', 3, NULL,                  NULL,                          'biz:gbActivity:add',  NULL,                 1,  1, 1, 0, 1),
  (522, 520, '编辑活动', 3, NULL,                  NULL,                          'biz:gbActivity:edit', NULL,                 2,  1, 1, 0, 1),
  (523, 520, '删除活动', 3, NULL,                  NULL,                          'biz:gbActivity:remove', NULL,               3,  1, 1, 0, 1),

  -- 订单/组队监控
  (530, 500, '拼单监控', 2, '/groupbuy/order',    '/groupbuy/order/index',       'biz:gbOrder:list',    'ListOutline',        3,  1, 1, 0, 1);

-- 给超级管理员(角色ID=1)授权全部新菜单（超管无通配符，必须显式授权）
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`) VALUES
  (9100, 1, 500),
  (9110, 1, 510), (9111, 1, 511), (9112, 1, 512), (9113, 1, 513), (9114, 1, 514),
  (9120, 1, 520), (9121, 1, 521), (9122, 1, 522), (9123, 1, 523),
  (9130, 1, 530);

SET FOREIGN_KEY_CHECKS = 1;
