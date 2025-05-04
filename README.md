# 档案管理系统

这是一个基于Spring Boot和Vue.js的档案管理系统。

## 项目结构

```
Archive/
├── backend/                 # 后端代码
│   ├── src/                 # 源代码
│   │   ├── main/            # 主要代码
│   │   │   ├── java/        # Java代码
│   │   │   └── resources/   # 配置文件
│   │   └── test/            # 测试代码
│   ├── pom.xml              # Maven配置
│   ├── mvnw                 # Maven Wrapper脚本(Unix)
│   ├── mvnw.cmd             # Maven Wrapper脚本(Windows)
│   ├── Dockerfile           # 后端Docker构建文件
│   └── ...
├── frontend/                # 前端代码
│   ├── src/                 # 源代码
│   │   ├── api/             # API调用
│   │   ├── assets/          # 静态资源
│   │   ├── components/      # Vue组件
│   │   ├── router/          # 路由配置
│   │   ├── stores/          # Pinia状态管理
│   │   ├── utils/           # 工具函数
│   │   └── views/           # 页面视图
│   ├── public/              # 公共文件
│   ├── index.html           # HTML入口
│   ├── package.json         # npm配置
│   ├── vite.config.js       # Vite配置
│   ├── .env.development     # 开发环境配置
│   ├── .env.production      # 生产环境配置
│   ├── Dockerfile           # 前端Docker构建文件
│   └── nginx.conf           # Nginx配置
├── uploads/                 # 上传文件存储目录
│   └── archives/            # 档案文件存储
├── docker-compose.yml       # Docker Compose配置
└── .gitignore               # Git忽略文件配置
```

## 启动应用

### 方式一：本地开发环境

#### 后端

1. 进入backend目录：
   ```
   cd backend
   ```

2. 使用Maven构建项目：
   ```
   ./mvnw clean package
   ```

3. 运行Spring Boot应用：
   ```
   ./mvnw spring-boot:run
   ```

后端服务将在 http://localhost:8080/archive 上运行。

#### 前端

1. 进入frontend目录：
   ```
   cd frontend
   ```

2. 安装依赖：
   ```
   npm install
   ```

3. 启动开发服务器：
   ```
   npm run dev
   ```

前端应用将在 http://localhost:8000 上运行。

### 方式二：使用Docker Compose

使用Docker Compose可以一键启动整个应用，包括后端、前端、MySQL和Redis：

```
docker-compose up -d
```

服务将在以下地址运行：
- 前端：http://localhost:8000
- 后端API：http://localhost:8080/archive
- MySQL：localhost:3306
- Redis：localhost:6379

## API访问路径

- 前端访问API的基础路径为: `/api/*`
- 后端控制器接收API请求的路径为: `/api/*`
- 在开发模式下，前端会将API请求转发到后端，不需要修改任何配置

例如，前端中的 `/api/archives` 请求会被转发到后端的 `http://localhost:8080/archive/api/archives` 端点。

## 开发指南

### 环境变量

前端开发环境使用 `.env.development` 文件中的配置，生产环境使用 `.env.production` 文件中的配置。

后端开发环境使用 `application.yml` 文件中的配置，Docker环境使用 `application-docker.yml` 文件中的配置。

### 数据库设置

默认数据库配置：
- 数据库名称：archive_db
- 用户名：root
- 密码：wznazj123!@#

初始化数据在 `backend/src/main/resources/db/init.sql` 文件中。

# archive---系统架构文档
基于SpringBoot+Vue3+mysql,利用Cursor实现的档案管理系统。
包括以下模块内容：
1. 系统概述
档案管理系统是一个基于Spring Boot的现代化档案管理平台，提供档案的电子化管理、查询、借阅等功能。
2. 技术栈
后端：Spring Boot 3.x
数据库：MySQL 8.0
缓存：Redis, Caffeine
搜索：Elasticsearch
消息队列：RabbitMQ
监控：Prometheus + Grafana
3. 系统模块
3.1 核心模块
档案管理
用户管理
权限管理
借阅管理
3.2 扩展模块
工作流引擎
报表系统
全文检索
审计日志
4. 部署架构
系统采用微服务架构，主要组件包括：

API网关
认证服务
业务服务
文件服务
消息服务

