# 📊 MQ Monitor

[![Version](https://img.shields.io/badge/Version-0.0.1--SNAPSHOT-blue)](https://img.shields.io/badge/Version-0.0.1--SNAPSHOT-blue)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://img.shields.io/badge/Java-21-orange)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen)](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen)
[![IBM MQ](https://img.shields.io/badge/IBM%20MQ-9.3.4.0-blue)](https://img.shields.io/badge/IBM%20MQ-9.3.4.0-blue)

MQ Monitor is a web application designed to monitor the status of IBM MQ queue managers, queues, and channels. It offers an intuitive dashboard and a RESTful API, enabling users to monitor the health of MQ resources in real-time. The system features both automatic and manual reconnection capabilities, ensuring a swift recovery when MQ connections are disrupted.

## 🌐 Available Languages

[![English](https://img.shields.io/badge/English-Click-yellow)](README_en.md)
[![繁體中文](https://img.shields.io/badge/繁體中文-Click-orange)](README.md)
[![简体中文](https://img.shields.io/badge/简体中文-Click-green)](README_zh-CN.md)

## ✨ Key Features

- **Queue Manager Monitoring**: Displays the connection status and uptime of the queue manager.
- **Queue Monitoring**: Shows the depth, utilization, and connection count for all queues.
- **Channel Monitoring**: Displays the status and activity of all channels.
- **Automatic Refresh**: Supports configurable automatic refresh intervals.
- **Automatic Reconnection**: Automatically attempts to reconnect when the MQ connection is lost.
- **Manual Reconnection**: Provides a web interface and API endpoint to manually trigger reconnection.
- **RESTful API**: Offers a comprehensive API for seamless integration with other systems.
- **Responsive Design**: Adapts to various screen sizes and devices.

## 🔧 Technical Architecture

- **Backend**:
  - Java 21
  - Spring Boot 3.4.5
  - IBM MQ Java Client 9.3.4.0
  - PCF (Programmable Command Format) API
  - Spring Scheduling (for automatic reconnection)
  - Lombok
  - Apache Commons Lang3

- **Frontend**:
  - FreeMarker Template Engine
  - Bootstrap 5
  - JavaScript

## 🖥️ System Requirements

- Java 21 or later
- Maven 3.6 or later
- IBM MQ Server (local or remote)

## 🚀 Installation and Setup

### 1. Get the Project

```bash
git clone https://github.com/yourusername/mq-monitor.git
cd mq-monitor
```

### 2. Configure IBM MQ Connection

Edit the `src/main/resources/application.yml` file to configure your IBM MQ connection details:

```yaml
# IBM MQ Configuration
mq-info:
  queueManager: YOUR_QUEUE_MANAGER_NAME
  channel: YOUR_CHANNEL_NAME
  connName: YOUR_HOST(YOUR_PORT)
  user: YOUR_USERNAME
  password: YOUR_PASSWORD  # Optional, if password authentication is required
```

### 3. Build and Package

```bash
./mvnw clean package
```

### 4. Run the Application

```bash
java -jar target/mq-monitor-0.0.1-SNAPSHOT.jar
```

Or, use Maven to run directly:

```bash
./mvnw spring-boot:run
```

The application will start at http://localhost:8080.

## 📝 Usage

### Web Interface

1. Open your browser and visit http://localhost:8080
2. The dashboard will display the status of the queue manager, queues, and channels.
3. You can configure the automatic refresh interval or manually refresh the page.
4. Click the "Reconnect MQ" button to manually trigger a reconnection.

### REST API

MQ Monitor provides the following REST API endpoints:

- **GET /api/mq/queuemanager** - Get the queue manager status
- **GET /api/mq/queues** - Get the status of all queues
- **GET /api/mq/channels** - Get the status of all channels
- **GET /api/mq/status** - Get the status of all MQ resources
- **POST /api/mq/reconnect** - Manually trigger a reconnection to MQ

Example requests:

```bash
# Get the status of all MQ resources
curl http://localhost:8080/api/mq/status

# Manually trigger a reconnection to MQ
curl -X POST http://localhost:8080/api/mq/reconnect
```

## 📘 API Documentation

### Queue Manager Status API

**Request**:
```
GET /api/mq/queuemanager
```

**Response**:
```json
{
  "name": "MQJ006D",
  "status": "Running",
  "connected": true,
  "startDate": "2023-05-15",
  "startTime": "08:30:45"
}
```

### Queue Status API

**Request**:
```
GET /api/mq/queues
```

**Response**:
```json
[
  {
    "name": "DEV.QUEUE.1",
    "depth": 10,
    "maxDepth": 5000,
    "openInputCount": 1,
    "openOutputCount": 2,
    "status": "OK"
  },
  ...
]
```

### Channel Status API

**Request**:
```
GET /api/mq/channels
```

**Response**:
```json
[
  {
    "name": "DEV.APP.SVRCONN",
    "status": "Running",
    "active": true
  },
  ...
]
```

### Reconnect API

**Request**:
```
POST /api/mq/reconnect
```

**Response**:
```json
{
  "success": true,
  "message": "Successfully reconnected to MQ"
}
```
Or
```json
{
  "success": false,
  "message": "Failed to reconnect to MQ"
}
```

## 📂 Project Structure

```
mq-monitor/
├── .mvn/                          # Maven Wrapper Directory
├── .vscode/                       # VS Code Configuration
├── src/
│   ├── main/
│   │   ├── java/com/example/mqmonitor/
│   │   │   ├── config/            # Configuration Classes
│   │   │   ├── controller/        # Controllers
│   │   │   │   ├── api/           # API Controllers
│   │   │   │   └── web/           # Web Controllers
│   │   │   ├── model/             # Data Models
│   │   │   ├── scheduler/         # Schedulers
│   │   │   ├── service/           # Service Layer
│   │   │   └── MqMonitorApplication.java
│   │   └── resources/
│   │       ├── META-INF/          # Additional Configuration Metadata
│   │       ├── templates/         # FreeMarker Templates
│   │       ├── application.yml    # Application Configuration
│   │       └── logback.xml        # Logging Configuration
│   └── test/                      # Test Classes
├── pom.xml                        # Maven Configuration
├── spec.md                        # Specification Document
├── todolist.md                    # To-Do List
└── README.md                      # This File
```

## 📋 File List

### Configuration Classes
- `MQConfig.java` - MQ connection configuration, responsible for creating and managing MQ connections.
- `MQInfoProperties.java` - MQ connection properties, reads MQ connection parameters from the configuration file.

### Controllers
- **API Controllers**
  - `MqApiController.java` - Provides RESTful API endpoints for getting MQ status and triggering reconnection.
- **Web Controllers**
  - `HomeController.java` - Handles homepage requests, displaying the MQ monitoring dashboard.
  - `ReportController.java` - Handles report generation and preview requests.

### Data Models
- `MQStatus.java` - MQ status model, containing status information for the queue manager, queues, and channels.

### Service Layer
- `MQConnectionService.java` - MQ connection service, responsible for managing the connection to MQ.
- `MQPCFService.java` - MQ PCF command service, uses PCF commands to get the status of MQ resources.
- `PdfReportService.java` - PDF report generation service, generates MQ status reports.

### Schedulers
- `MQConnectionScheduler.java` - MQ connection check scheduler, periodically checks the MQ connection status and automatically reconnects.

### Resource Files
- `application.yml` - Application configuration file, containing MQ connection and FreeMarker configurations.
- `logback.xml` - Logging configuration file.
- `index.ftl` - Homepage FreeMarker template.
- `pdf-viewer.ftl` - PDF preview page FreeMarker template.

### Documentation Files
- `README.md` - Project documentation.
- `spec.md` - System specification document.
- `report.md` - Development report.
- `todolist.md` - To-do list.

## 👨‍💻 Development and Contribution

### Development Environment Setup

1. Ensure you have Java 21 and Maven installed.
2. Clone the project and import it into your IDE (Eclipse, IntelliJ IDEA, etc.).
3. Run `MqMonitorApplication.java` to start the application.

### Suggestions and Contributions

1. Fork the project.
2. Create your feature branch (`git checkout -b feature/amazing-feature`).
3. Commit your changes (`git commit -m 'Add some amazing feature'`).
4. Push to the branch (`git push origin feature/amazing-feature`).
5. Open a Pull Request.

## 📜 License Information

This project is licensed under the [Apache License 2.0](LICENSE).

## 📞 Contact

For any questions or suggestions, please contact:

- GitHub Issues: [Submit an issue](https://github.com/vancetang/mq-monitor/issues)
