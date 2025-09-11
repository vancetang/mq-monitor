# 📊 MQ Monitor

![版本](https://img.shields.io/badge/版本-0.0.1--SNAPSHOT-blue)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen)
![IBM MQ](https://img.shields.io/badge/IBM%20MQ-9.3.4.0-blue)

MQ Monitor 是一個用於監控 IBM MQ 佇列管理器、佇列和通道狀態的 Web 應用程式。它提供了直觀的儀表板和 RESTful API，讓使用者能夠即時監控 MQ 資源的運行狀況。系統具備自動和手動重新連線功能，確保在 MQ 連線斷開時能夠快速恢復連線。

## 🌐 可用語言

[![English](https://img.shields.io/badge/English-Click-yellow)](README_en.md)
[![繁體中文](https://img.shields.io/badge/繁體中文-Click-orange)](README.md)
[![简体中文](https://img.shields.io/badge/简体中文-Click-green)](README_zh-CN.md)

## ✨ 功能特點

- **佇列管理器監控**：顯示佇列管理器的連接狀態、啟動時間等資訊
- **佇列監控**：顯示所有佇列的深度、使用率、連接數等資訊
- **通道監控**：顯示所有通道的狀態和活動情況
- **自動重新整理**：支援可配置的自動重新整理間隔
- **自動重新連線**：當 MQ 連線斷開時，系統會自動嘗試重新連線
- **手動重新連線**：提供 Web 界面和 API 端點，允許手動觸發重新連線
- **RESTful API**：提供完整的 API 介面，方便與其他系統整合
- **響應式設計**：適應不同螢幕尺寸的裝置

## 🔧 技術架構

- **後端**：
  - Java 21
  - Spring Boot 3.4.5
  - IBM MQ Java Client 9.3.4.0
  - PCF (Programmable Command Format) API
  - Spring Scheduling (用於自動重新連線)
  - Lombok
  - Apache Commons Lang3

- **前端**：
  - FreeMarker 模板引擎
  - Bootstrap 5
  - JavaScript

## 🖥️ 系統需求

- Java 21 或更高版本
- Maven 3.6 或更高版本
- IBM MQ 伺服器 (本地或遠端)

## 🚀 安裝與設定

### 1. 取得專案

```bash
git clone https://github.com/yourusername/mq-monitor.git
cd mq-monitor
```

### 2. 配置 IBM MQ 連接

編輯 `src/main/resources/application.yml` 檔案，設定您的 IBM MQ 連接資訊：

```yaml
# IBM MQ 配置
mq-info:
  queueManager: YOUR_QUEUE_MANAGER_NAME
  channel: YOUR_CHANNEL_NAME
  connName: YOUR_HOST(YOUR_PORT)
  user: YOUR_USERNAME
  password: YOUR_PASSWORD  # 可選，如果需要密碼認證
```

### 3. 編譯與打包

```powershell
mvn clean package
```

說明：本專案採用 WAR 打包（外部容器部署），本地開發建議使用 Spring Boot 的 embedded 模式：

### 4. 本地開發啟動（Embedded Tomcat）

```powershell
mvn spring-boot:run
```

應用程式將在 http://localhost:8080 啟動。

### 5. 外部容器部署（Tomcat / IBM Liberty）
- 產物：`target\mq-monitor-0.0.1-SNAPSHOT.war`
- Tomcat：複製 WAR 至 `CATALINA_BASE\webapps` 或使用管理工具部署
- IBM Liberty：複製 WAR 至 `wlp\usr\servers\<YourServer>\dropins` 或依 server.xml 配置的 apps 目錄

## Tomcat 與 IBM Liberty 的 JNDI 設定與 Debug 指南

### Spring Boot：建議使用 profile（container）管理 JNDI 設定
- 建立 `src/main/resources/application-container.yml` 並啟用 profile `container`（見下方「啟用 Spring Profile」）。
- 內容：

```yaml
spring:
  datasource:
    jndi-name: java:comp/env/jndi/eltwdb
```

此作法讓預設（embedded）啟動不會嘗試 JNDI 查詢；僅在容器（Tomcat/Liberty）佈署或你手動啟用 profile 時才啟用 JNDI。

### Tomcat 設定
1) 將 DB2 JDBC Driver jar 置於 `CATALINA_BASE\lib`
2) 在 `conf/server.xml` 的 `GlobalNamingResources` 宣告 DataSource：

```xml
<GlobalNamingResources>
  <Resource name="jndi/eltwdb" auth="Container"
    type="javax.sql.DataSource" driverClassName="com.ibm.db2.jcc.DB2Driver"
    url="jdbc:db2://127.0.0.1:50000/eltw"
    username="vance" password="1qaz2wsx"
    maxTotal="50" maxIdle="10" validationQuery="SELECT 1 FROM SYSIBM.SYSDUMMY1"/>
</GlobalNamingResources>
```

3) 在 `conf/context.xml` 建立 ResourceLink（讓 WebApp 取用）：

```xml
<Context>
  <ResourceLink name="jndi/eltwdb" global="jndi/eltwdb"
    type="javax.sql.DataSource"/>
</Context>
```

4) 啟動與 Debug（JPDA，PowerShell）：

```powershell
$env:JPDA_ADDRESS="*:8000" && $env:JPDA_TRANSPORT="dt_socket" && .\bin\catalina.bat jpda start
```

5) spring-boot-starter-tomcat 的 jar 實體路徑（僅嵌入式情境用）：
- Windows 本機 Maven 倉庫：`C:\Users\<YourUser>\.m2\repository`
  - `org\springframework\boot\spring-boot-starter-tomcat\<version>\spring-boot-starter-tomcat-<version>.jar`
  - 內嵌 Tomcat：`org\apache\tomcat\embed\tomcat-embed-core\<version>\tomcat-embed-core-<version>.jar`

### IBM Liberty 設定
1) 將 DB2 Driver jar 置於 `${server.config.dir}/resources`
2) `server.xml` 定義 library 與 dataSource（與 Tomcat 使用同一個 JNDI 名稱）：

```xml
<library id="eltwdbLib">
  <fileset dir="${server.config.dir}/resources"/>
</library>
<dataSource jndiName="jndi/eltwdb">
  <jdbcDriver libraryRef="eltwdbLib"/>
  <properties serverName="127.0.0.1" portNumber="50000"
    databaseName="eltw" user="vance" password="1qaz2wsx" driverType="4"/>
</dataSource>
```

> 提示：請依實際情況啟用相容的 Liberty features（例如 `servlet-6.0`、`jdbc-4.3` 等）。

### 啟用 Spring Profile（container）
- 外部 Tomcat：`bin\setenv.bat` 增加

```bat
set "CATALINA_OPTS=%CATALINA_OPTS% -Dspring.profiles.active=container"
```

- IBM Liberty：`${server.config.dir}/jvm.options` 增加

```
-Dspring.profiles.active=container
```


### Liberty Maven Plugin 使用

- 開發熱重載（Dev Mode）
```powershell
mvn liberty:dev
```

- 建立/啟動/停止伺服器（非前景）
```powershell
mvn liberty:create && mvn liberty:start
mvn liberty:stop
```

- 部署/移除 WAR
```powershell
mvn -DskipTests package && mvn liberty:deploy
mvn liberty:undeploy
```

注意：目前已加入外掛 3.11.5，但尚未設定 runtimeArtifact（自動下載 Liberty）。
- 若你本機已安裝 Liberty，以上指令可直接使用本機安裝。
- 若需外掛自動下載，請提供 runtimeArtifact 座標（groupId/artifactId/version/type=zip），我會補上到 pom.xml。

## 📝 使用方法

### Web 介面

1. 開啟瀏覽器，訪問 http://localhost:8080
2. 儀表板將顯示佇列管理器、佇列和通道的狀態
3. 可以設定自動重新整理間隔或手動重新整理
4. 點擊「重新連接 MQ」按鈕可手動觸發重新連線

### REST API

MQ Monitor 提供以下 REST API 端點：

- **GET /api/mq/queuemanager** - 獲取佇列管理器狀態
- **GET /api/mq/queues** - 獲取所有佇列狀態
- **GET /api/mq/channels** - 獲取所有通道狀態
- **GET /api/mq/status** - 獲取所有 MQ 資源的狀態
- **POST /api/mq/reconnect** - 手動觸發重新連接到 MQ

範例請求：

```bash
# 獲取所有 MQ 資源狀態
curl http://localhost:8080/api/mq/status

# 手動觸發重新連接到 MQ
curl -X POST http://localhost:8080/api/mq/reconnect
```

## 📘 API 文檔

### 佇列管理器狀態 API

**請求**：
```
GET /api/mq/queuemanager
```

**回應**：
```json
{
  "name": "MQJ006D",
  "status": "正常運行",
  "connected": true,
  "startDate": "2023-05-15",
  "startTime": "08:30:45"
}
```

### 佇列狀態 API

**請求**：
```
GET /api/mq/queues
```

**回應**：
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

### 通道狀態 API

**請求**：
```
GET /api/mq/channels
```

**回應**：
```json
[
  {
    "name": "DEV.APP.SVRCONN",
    "status": "運行中",
    "active": true
  },
  ...
]
```

### 重新連接 API

**請求**：
```
POST /api/mq/reconnect
```

**回應**：
```json
{
  "success": true,
  "message": "已成功重新連接到 MQ"
}
```
或
```json
{
  "success": false,
  "message": "重新連接到 MQ 失敗"
}
```

## 📂 專案結構

```
mq-monitor/
├── .mvn/                          # Maven 包裝器目錄
├── .vscode/                       # VS Code 配置
├── src/
│   ├── main/
│   │   ├── java/com/example/mqmonitor/
│   │   │   ├── config/            # 配置類
│   │   │   ├── controller/        # 控制器
│   │   │   │   ├── api/           # API 控制器
│   │   │   │   └── web/           # Web 控制器
│   │   │   ├── model/             # 資料模型
│   │   │   ├── scheduler/         # 排程器
│   │   │   ├── service/           # 服務層
│   │   │   └── MqMonitorApplication.java
│   │   └── resources/
│   │       ├── META-INF/          # 額外配置元數據
│   │       ├── templates/         # FreeMarker 模板
│   │       ├── application.yml    # 應用程式配置
│   │       └── logback.xml        # 日誌配置
│   └── test/                      # 測試類
├── pom.xml                        # Maven 配置
├── spec.md                        # 規格文件
├── todolist.md                    # 任務清單
└── README.md                      # 本文件
```

## 📋 檔案清單

### 配置類
- `MQConfig.java` - MQ 連接配置，負責創建和管理 MQ 連接
- `MQInfo.java` - MQ 連接屬性，從配置文件中讀取 MQ 連接參數

### 控制器
- **API 控制器**
  - `MqApiController.java` - 提供 RESTful API 端點，用於獲取 MQ 狀態和觸發重新連線
- **Web 控制器**
  - `HomeController.java` - 處理主頁請求，顯示 MQ 監控儀表板
  - `ReportController.java` - 處理報表生成和預覽請求

### 資料模型
- `MQStatus.java` - MQ 狀態模型，包含佇列管理器、佇列和通道的狀態資訊

### 服務層
- `MQConnectionService.java` - MQ 連接服務，負責管理與 MQ 的連接
- `MQPCFService.java` - MQ PCF 命令服務，使用 PCF 命令獲取 MQ 資源狀態
- `PdfReportService.java` - PDF 報表生成服務，生成 MQ 狀態報表

### 排程器
- `MQConnectionScheduler.java` - MQ 連接檢查排程器，定期檢查 MQ 連接狀態並自動重新連線

### 資源文件
- `application.yml` - 應用程式配置文件，包含 MQ 連接和 FreeMarker 配置
- `logback.xml` - 日誌配置文件
- `index.ftl` - 主頁 FreeMarker 模板
- `pdf-viewer.ftl` - PDF 預覽頁面 FreeMarker 模板

### 文檔文件
- `README.md` - 專案說明文件
- `spec.md` - 系統規格文件
- `report.md` - 開發報告
- `todolist.md` - 任務清單

## 👨‍💻 開發與貢獻

### 開發環境設定

1. 確保您已安裝 Java 21 和 Maven
2. 克隆專案並導入到您的 IDE (Eclipse, IntelliJ IDEA 等)
3. 執行 `MqMonitorApplication.java` 啟動應用程式

### 建議與貢獻

1. Fork 專案
2. 創建您的功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交您的更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 開啟一個 Pull Request

## 📜 授權資訊

本專案採用 [Apache License 2.0](LICENSE)。

## 📞 聯絡方式

如有任何問題或建議，請聯絡：

- GitHub Issues：[提交問題](https://github.com/vancetang/mq-monitor/issues)
