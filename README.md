# WebSocket文本编辑器 Docker部署指南

## 项目概述

这是一个基于Spring Boot的协作式文本编辑器，支持实时协同编辑功能。项目使用以下技术栈：

- 后端：Spring Boot 3.5.4 (Java 17)
- 前端：Thymeleaf模板引擎 + Monaco Editor
- 数据库：PostgreSQL
- 实时通信：Y.js + WebSocket
- 消息队列：RabbitMQ

## Docker部署

### 1. 环境准备

1. 复制 `.env.example` 文件为 `.env` 并根据需要修改配置：
   ```bash
   cp .env.example .env
   ```

2. 修改 `.env` 文件中的敏感信息（如数据库密码等）

### 2. 构建和启动服务

使用docker-compose一键部署所有服务：

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 3. 服务架构

- **backend** (端口: 8080): Spring Boot应用服务
- **ywebsocket** (端口: 1234): Y.js WebSocket服务，用于实时协同编辑
- **postgres** (端口: 5432): PostgreSQL数据库
- **rabbitmq** (端口: 5672/15672): RabbitMQ消息队列
- **elasticsearch** (端口: 9200/9300): Elasticsearch搜索引擎
- **logstash** (端口: 5044/9600): Logstash日志收集处理
- **kibana** (端口: 5601): Kibana数据可视化
- **prometheus** (端口: 9090): Prometheus监控系统

### 4. 访问应用

启动成功后，可以通过以下地址访问：

- 应用程序：http://localhost:8080
- RabbitMQ管理界面：http://localhost:15672
- Kibana：http://localhost:5601
- Prometheus：http://localhost:9090

### 5. 停止服务

```bash
# 停止所有服务
docker-compose down

# 停止并删除数据卷（谨慎操作）
docker-compose down -v
```

## 开发说明

### 目录结构

```
.
├── src/                  # Spring Boot应用源代码
├── y-websocket/          # Y.js WebSocket服务
├── uploads/              # 用户上传文件目录（头像等）
├── logs/                 # 应用程序日志目录
├── logstash/             # Logstash配置
│   ├── config/           # Logstash配置文件
│   └── pipeline/         # Logstash管道配置
├── prometheus/           # Prometheus配置
│   └── prometheus.yml    # Prometheus配置文件
├── Dockerfile            # Spring Boot应用Docker配置
├── docker-compose.yml    # Docker服务编排配置
└── .env.example         # 环境变量示例文件
```

### 数据持久化

以下数据会被持久化存储：

1. PostgreSQL数据库数据
2. RabbitMQ配置和消息数据
3. 用户上传的头像文件

### ELK日志收集和监控

系统集成了ELK（Elasticsearch, Logstash, Kibana）堆栈用于日志收集和可视化：

- **Elasticsearch**: 存储和索引应用程序日志
- **Logstash**: 收集、解析和转发日志到Elasticsearch
- **Kibana**: 提供日志数据的可视化界面

### Prometheus监控

系统集成了Prometheus用于应用性能监控：

- 通过Spring Boot Actuator暴露metrics端点
- Prometheus定期抓取并存储监控数据
- 可通过Prometheus UI进行查询和告警

### 注意事项

1. 首次启动时，数据库会自动初始化
2. 应用会自动创建数据库表结构
3. 请确保端口未被占用
4. 生产环境部署时，请修改默认密码
5. ELK和Prometheus服务需要额外的系统资源，请确保有足够的内存和CPU