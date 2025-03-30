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

