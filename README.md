<div align="center">

# 集萃 GATHER

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green?style=flat-square&logo=springboot)
![Vue](https://img.shields.io/badge/Vue-3.4-brightgreen?style=flat-square&logo=vue.js)
![Naive UI](https://img.shields.io/badge/Naive%20UI-2.37-blue?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)

**基于 Spring Boot 3 + Vue 3 的现代化后台管理与拼团交易系统**

</div>

## 项目简介

集萃 GATHER 是一套前后端分离的全栈项目，包含企业级后台管理系统、拼团交易营销模块、PC 端拼团页面与移动端小程序。后端采用分层架构（common / infra / core / api），提供完整的权限管理、系统监控、消息推送、即时通讯、拼团交易等能力。

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.2 | 基础框架 |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| Sa-Token | 1.37.0 | 权限认证框架 |
| Redis | 7.0+ | 缓存/会话存储 |
| MySQL | 8.0+ | 数据库 |
| RabbitMQ | - | 消息队列（拼团成团/失败通知，可选） |
| Quartz | 2.3.2 | 定时任务框架 |
| Hutool | 5.8.25 | Java 工具类库 |
| MinIO / 阿里云 OSS | - | 对象存储（可选） |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.15 | 前端框架 |
| Vite | 5.0.11 | 构建工具 |
| TypeScript | 5.3.3 | 类型安全 |
| Naive UI | 2.37.3 | 后台 UI 组件库 |
| Pinia | 2.1.7 | 状态管理 |
| Vue Router | 4.2.5 | 路由管理 |
| Axios | 1.6.5 | HTTP 客户端 |
| ECharts | 6.0.0 | 图表库 |

### 移动端（小程序）

| 技术 | 版本 | 说明 |
|------|------|------|
| UniApp | - | 跨平台框架 |
| uView Plus | 3.3.36 | UI 组件库 |
| crypto-js | 4.2.0 | 加密工具 |

## 项目结构

```
集萃
├── campus-common              # 公共基础模块（实体、异常、统一响应、工具类）
├── campus-infra               # 基础设施层
│   ├── campus-db              # 数据库配置
│   ├── campus-redis           # Redis 配置
│   ├── campus-oss             # 文件存储（本地/MinIO/阿里云OSS）
│   ├── campus-sms             # 短信服务
│   ├── campus-pay             # 支付服务（微信/支付宝）
│   ├── campus-push            # 推送服务
│   ├── campus-social          # 社交登录
│   ├── campus-wechat          # 微信公众号/小程序
│   ├── campus-websocket       # WebSocket 支持
│   └── campus-crypto          # 加密解密
│
├── campus-core                # 业务核心层
│   ├── campus-system          # 系统管理（用户/角色/菜单等）
│   ├── campus-auth            # 认证授权（密码/短信/社交/小程序登录策略）
│   ├── campus-file            # 文件管理
│   ├── campus-gen             # 代码生成
│   ├── campus-message         # 消息中心（公告/聊天/群聊）
│   └── campus-biz             # 拼团交易业务
│
├── campus-api                 # 接口层
│   ├── campus-admin-api       # 后台管理接口
│   ├── campus-app-api         # 移动端接口
│   └── campus-web-api         # PC 网页端接口
│
├── campus-job                 # 定时任务模块（Quartz）
├── campus-starter             # 启动入口（application*.yml）
│
├── campus-ui                  # 后台管理前端（Vue 3 + Naive UI）
├── campus-web                 # PC 端拼团页面（Vue 3）
├── campus-uniapp              # 移动端小程序（UniApp）
│
└── sql                        # 数据库脚本
    ├── campus-system.sql      # 系统基础库
    ├── groupbuy.sql           # 拼团交易模块表结构
    └── seed_groupbuy_testdata.sql  # 拼团测试数据
```

## 功能特性

### 系统管理
- 用户管理、角色管理、菜单管理、部门管理、岗位管理
- 数据字典维护、系统配置（分组管理）

### 系统监控
- 在线用户、定时任务、服务监控（CPU/内存/JVM）、缓存监控

### 日志管理
- 登录日志、操作日志（AOP 切面自动记录）

### 消息中心
- 系统公告、即时通讯（WebSocket）、私聊、群聊

### 拼团交易
- 拼团活动配置、成团/未成团流程、状态机驱动
- 成团/失败通知（RabbitMQ 可选，关闭时回落本地消息表）
- 超时未成团扫描、通知补偿扫描

### 文件管理
- 文件上传（本地/MinIO/阿里云OSS）、文件列表与预览

### 代码生成
- 根据数据库表生成前后端代码

### 安全特性
- 图片/滑块/短信验证码、RSA 接口加密传输
- 登录失败限制、基于 RBAC 的细粒度权限控制
- 多种登录方式（密码/短信/社交/小程序）

## 快速开始

### 环境准备

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.0+
- Node.js 18+

### 后端启动

1. 初始化数据库

```sql
CREATE DATABASE `campus-system` DEFAULT CHARACTER SET utf8mb4;
```

```bash
mysql -u root -p campus-system < sql/campus-system.sql
mysql -u root -p campus-system < sql/groupbuy.sql
```

2. 修改 `campus-starter/src/main/resources/application-dev.yml` 中的数据库和 Redis 配置。

3. 启动项目

```bash
mvn clean install
cd campus-starter
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8080`。

> 生产环境配置 `application-prod.yml` 中的密码通过环境变量注入（`DB_PASSWORD`、`REDIS_PASSWORD` 等），仓库内不保存明文密码。

### 后台前端启动

```bash
cd campus-ui
npm install
npm run dev
```

### PC 拼团页面启动

```bash
cd campus-web
npm install
npm run dev
```

### 移动端启动

使用 HBuilderX 打开 `campus-uniapp` 目录，运行到微信开发者工具。

> 修改 `campus-uniapp` 源码后需执行 `npm run build:mp-weixin` 重新构建，开发者工具才能看到最新产物。

## 开发指南

### 添加操作日志

```java
@Log(title = "用户管理", businessType = BusinessType.INSERT)
@PostMapping
public Result<Void> add(@RequestBody SysUser user) {
    // ...
}
```

### 添加权限控制

```java
@SaCheckPermission("system:user:add")
@PostMapping
public Result<Void> add(@RequestBody SysUser user) {
    // ...
}
```

### 添加新的登录方式

在 `campus-core/campus-auth/strategy` 下实现 `LoginStrategy` 接口。

## 开源协议

本项目基于 [MIT License](LICENSE) 开源。
