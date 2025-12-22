# MQ Monitor JNDI 設定完整指南

## 問題說明

### 1. WAR檔案中的 lib-provided 資料夾
您的WAR檔案中出現 `WEB-INF/lib-provided/` 資料夾是**正常且正確的**！

**原因：**
- Spring Boot Maven Plugin會將scope為`provided`的依賴放入`lib-provided`目錄
- 這些JAR檔案不會被容器載入到運行時classpath
- 這正是我們想要的行為，因為這些依賴應該由外部容器提供

**包含的檔案：**
- `tomcat-embed-core-10.1.44.jar` - 內嵌Tomcat核心（由IBM Liberty提供servlet容器功能）
- `tomcat-embed-websocket-10.1.44.jar` - WebSocket支援
- `jcc-12.1.2.0.jar` - DB2 JDBC Driver（由容器提供）

### 2. 本地端JNDI測試需求
您希望本地開發時也使用JNDI方式獲取DataSource。

## 解決方案

### 方案1：外部Tomcat開發（推薦）

#### 優點：
- 完全模擬生產環境
- 支援熱部署和Debug
- JNDI配置與IBM Liberty一致

#### 設定步驟：
1. 參考 `local-tomcat-setup.md` 檔案進行詳細設定
2. 使用以下指令啟動：

```powershell
# 一般啟動
mvn clean package -DskipTests
Copy-Item "target\mq-monitor-0.0.1-SNAPSHOT.war" "$env:CATALINA_HOME\webapps\"
cd $env:CATALINA_HOME\bin
.\startup.bat

# Debug模式啟動
$env:JPDA_ADDRESS="*:8000"
$env:JPDA_TRANSPORT="dt_socket"
.\catalina.bat jpda start
```

### 方案2：內嵌Tomcat + JNDI模擬

#### 使用場景：
- 快速開發測試
- 不想安裝外部Tomcat
- 需要IDE整合除錯

#### 啟動方式：
```powershell
# 使用embedded-jndi profile
mvn spring-boot:run -Dspring-boot.run.profiles=embedded-jndi

# 或設定環境變數
$env:SPRING_PROFILES_ACTIVE="embedded-jndi"
mvn spring-boot:run
```

### 方案3：混合模式開發

#### 開發階段配置：
```yaml
# application-dev.yml
spring:
  datasource:
    # 直接配置DataSource，不使用JNDI
    url: jdbc:db2://127.0.0.1:50000/eltw
    username: vance
    password: 1qaz2wsx
    driver-class-name: com.ibm.db2.jcc.DB2Driver
```

#### 容器部署配置：
```yaml
# application-container.yml (現有)
spring:
  datasource:
    jndi-name: java:comp/env/jndi/eltwdb
```

## 各種啟動方式對照表

| 啟動方式 | Profile | DataSource來源 | 適用場景 |
|---------|---------|---------------|----------|
| `mvn spring-boot:run` | 預設 | 無DataSource | MQ監控功能測試 |
| `mvn spring-boot:run -Dspring-boot.run.profiles=dev` | dev | 直接配置 | 快速開發 |
| `mvn spring-boot:run -Dspring-boot.run.profiles=embedded-jndi` | embedded-jndi | 內嵌JNDI | JNDI功能測試 |
| 外部Tomcat + container profile | container | 外部JNDI | 生產環境模擬 |
| IBM Liberty部署 | container | Liberty JNDI | 正式部署 |

## 建議的開發流程

### 1. 日常開發
```powershell
# 使用dev profile進行快速開發
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 2. JNDI功能測試
```powershell
# 使用內嵌JNDI進行測試
mvn spring-boot:run -Dspring-boot.run.profiles=embedded-jndi
```

### 3. 部署前測試
```powershell
# 使用外部Tomcat進行完整測試
mvn clean package -DskipTests
# 部署到外部Tomcat並啟用container profile
```

### 4. 正式部署
```powershell
# 打包並部署到IBM Liberty
mvn clean package -DskipTests
# 部署WAR檔案到Liberty，自動使用container profile
```

## 常見問題排除

### Q: 為什麼WAR檔案這麼大？
A: 這是正常的，因為包含了所有運行時依賴。`lib-provided`中的檔案不會影響運行。

### Q: 如何確認JNDI配置正確？
A: 檢查應用程式啟動日誌，應該會看到DataSource初始化成功的訊息。

### Q: 內嵌模式無法連接資料庫怎麼辦？
A: 檢查DB2資料庫是否啟動，以及連接參數是否正確。

### Q: 外部Tomcat部署後404錯誤？
A: 確認WAR檔案名稱和訪問路徑是否一致，通常是 `http://localhost:8080/mq-monitor-0.0.1-SNAPSHOT/`
