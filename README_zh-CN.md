# 📊 MQ 监控

![版本](https://img.shields.io/badge/版本-0.0.1--SNAPSHOT-blue)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen)
![IBM MQ](https://img.shields.io/badge/IBM%20MQ-9.3.4.0-blue)

MQ Monitor 是一个用于监控 IBM MQ 队列管理器、队列和通道状态的 Web 应用程序。它提供了直观的仪表盘和 RESTful API，让用户能够实时监控 MQ 资源的运行状况。系统具备自动和手动重连功能，确保在 MQ 连接断开时能够快速恢复连接。

## 🌐 可用语言

[![English](https://img.shields.io/badge/English-Click-yellow)](README_en.md)
[![繁體中文](https://img.shields.io/badge/繁體中文-Click-orange)](README.md)
[![简体中文](https://img.shields.io/badge/简体中文-Click-green)](README_zh-CN.md)

## ✨ 功能特点

- **队列管理器监控**：显示队列管理器的连接状态、启动时间等信息
- **队列监控**：显示所有队列的深度、使用率、连接数等信息
- **通道监控**：显示所有通道的状态和活动情况
- **自动刷新**：支持可配置的自动刷新间隔
- **自动重连**：当 MQ 连接断开时，系统会自动尝试重新连接
- **手动重连**：提供 Web 界面和 API 端点，允许手动触发重新连接
- **RESTful API**：提供完整的 API 接口，方便与其他系统集成
- **响应式设计**：适应不同屏幕尺寸的设备

## 🔧 技术架构

- **后端**：
  - Java 21
  - Spring Boot 3.4.5
  - IBM MQ Java Client 9.3.4.0
  - PCF (Programmable Command Format) API
  - Spring Scheduling (用于自动重连)
  - Lombok
  - Apache Commons Lang3

- **前端**：
  - FreeMarker 模板引擎
  - Bootstrap 5
  - JavaScript

## 🖥️ 系统需求

- Java 21 或更高版本
- Maven 3.6 或更高版本
- IBM MQ 服务器 (本地或远程)

## 🚀 安装与配置

### 1. 获取项目

```bash
git clone https://github.com/yourusername/mq-monitor.git
cd mq-monitor
```

### 2. 配置 IBM MQ 连接

编辑 `src/main/resources/application.yml` 文件，设置您的 IBM MQ 连接信息：

```yaml
# IBM MQ 配置
mq-info:
  queueManager: YOUR_QUEUE_MANAGER_NAME
  channel: YOUR_CHANNEL_NAME
  connName: YOUR_HOST(YOUR_PORT)
  user: YOUR_USERNAME
  password: YOUR_PASSWORD  # 可选，如果需要密码认证
```

### 3. 编译与打包

```bash
./mvnw clean package
```

### 4. 运行应用程序

```bash
java -jar target/mq-monitor-0.0.1-SNAPSHOT.jar
```

或使用 Maven 直接运行：

```bash
./mvnw spring-boot:run
```

应用程序将在 http://localhost:8080 启动。

## 📝 使用方法

### Web 界面

1. 打开浏览器，访问 http://localhost:8080
2. 仪表盘将显示队列管理器、队列和通道的状态
3. 可以设置自动刷新间隔或手动刷新
4. 点击“重新连接 MQ”按钮可手动触发重连

### REST API

MQ Monitor 提供以下 REST API 端点：

- **GET /api/mq/queuemanager** - 获取队列管理器状态
- **GET /api/mq/queues** - 获取所有队列状态
- **GET /api/mq/channels** - 获取所有通道状态
- **GET /api/mq/status** - 获取所有 MQ 资源的状态
- **POST /api/mq/reconnect** - 手动触发重新连接到 MQ

示例请求：

```bash
# 获取所有 MQ 资源状态
curl http://localhost:8080/api/mq/status

# 手动触发重新连接到 MQ
curl -X POST http://localhost:8080/api/mq/reconnect
```

## 📘 API 文档

### 队列管理器状态 API

**请求**：
```
GET /api/mq/queuemanager
```

**回应**：
```json
{
  "name": "MQJ006D",
  "status": "正常运行",
  "connected": true,
  "startDate": "2023-05-15",
  "startTime": "08:30:45"
}
```

### 队列状态 API

**请求**：
```
GET /api/mq/queues
```

**回应**：
```json
[
  {
    "name": "DEV.QUEUE.1",
    "depth": 10,
    "maxDepth": 5000,
    "openInputCount": 1,
    "openOutputCount": 2,
    "status": "正常"
  },
  ...
]
```

### 通道状态 API

**请求**：
```
GET /api/mq/channels
```

**回应**：
```json
[
  {
    "name": "DEV.APP.SVRCONN",
    "status": "运行中",
    "active": true
  },
  ...
]
```

### 重新连接 API

**请求**：
```
POST /api/mq/reconnect
```

**回应**：
```json
{
  "success": true,
  "message": "已成功重新连接到 MQ"
}
```
或
```json
{
  "success": false,
  "message": "重新连接到 MQ 失败"
}
```

## 📂 项目结构

```
mq-monitor/
├── .mvn/                          # Maven 包装器目录
├── .vscode/                       # VS Code 配置
├── src/
│   ├── main/
│   │   ├── java/com/example/mqmonitor/
│   │   │   ├── config/            # 配置类
│   │   │   ├── controller/        # 控制器
│   │   │   │   ├── api/           # API 控制器
│   │   │   │   └── web/           # Web 控制器
│   │   │   ├── model/             # 数据模型
│   │   │   ├── scheduler/         # 调度器
│   │   │   ├── service/           # 服务层
│   │   │   └── MqMonitorApplication.java
│   │   └── resources/
│   │       ├── META-INF/          # 额外配置元数据
│   │       ├── templates/         # FreeMarker 模板
│   │       ├── application.yml    # 应用程序配置
│   │       └── logback.xml        # 日志配置
│   └── test/                      # 测试类
├── pom.xml                        # Maven 配置
├── spec.md                        # 规格文件
├── todolist.md                    # 任务清单
└── README.md                      # 本文件
```

## 📋 文件清单

### 配置类
- `MQConfig.java` - MQ 连接配置，负责创建和管理 MQ 连接
- `MQInfoProperties.java` - MQ 连接属性，从配置文件中读取 MQ 连接参数

### 控制器
- **API 控制器**
  - `MqApiController.java` - 提供 RESTful API 端点，用于获取 MQ 状态和触发重新连接
- **Web 控制器**
  - `HomeController.java` - 处理主页请求，显示 MQ 监控仪表盘
  - `ReportController.java` - 处理报表生成和预览请求

### 数据模型
- `MQStatus.java` - MQ 状态模型，包含队列管理器、队列和通道的状态信息

### 服务层
- `MQConnectionService.java` - MQ 连接服务，负责管理与 MQ 的连接
- `MQPCFService.java` - MQ PCF 命令服务，使用 PCF 命令获取 MQ 资源状态
- `PdfReportService.java` - PDF 报表生成服务，生成 MQ 状态报表

### 调度器
- `MQConnectionScheduler.java` - MQ 连接检查调度器，定期检查 MQ 连接状态并自动重新连接

### 资源文件
- `application.yml` - 应用程序配置文件，包含 MQ 连接和 FreeMarker 配置
- `logback.xml` - 日志配置文件
- `index.ftl` - 主页 FreeMarker 模板
- `pdf-viewer.ftl` - PDF 预览页面 FreeMarker 模板

### 文档文件
- `README.md` - 项目说明文件
- `spec.md` - 系统规格文件
- `report.md` - 开发报告
- `todolist.md` - 任务清单

## 👨‍💻 开发与贡献

### 开发环境设置

1. 确保您已安装 Java 21 和 Maven
2. 克隆项目并导入到您的 IDE (Eclipse, IntelliJ IDEA 等)
3. 运行 `MqMonitorApplication.java` 启动应用程序

### 建议与贡献

1. Fork 项目
2. 创建您的功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交您的更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 开启一个 Pull Request

## 📜 授权信息

本项目采用 [MIT 授权](LICENSE)。

## 📞 联系方式

如有任何问题或建议，请联系：

- GitHub Issues：[提交问题](https://github.com/vancetang/mq-monitor/issues)
