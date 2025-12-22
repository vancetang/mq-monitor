
# 功能 Q & A

## WAR 檔案沒有 web.xml 是否會影響發佈到 IBM Liberty？
答案：**不會影響**

原因如下：
### Servlet 3.0+ 規範支援
- IBM Liberty 完全支援 Servlet 3.0+ 規範
- Spring Boot 使用註解驅動配置（`@SpringBootApplication`、`@WebServlet` 等）
- 不需要傳統的 web.xml 檔案

## Spring Boot 的自動配置機制
Spring Boot 會自動註冊以下重要組件：
- `DispatcherServlet`：處理所有 HTTP 請求
- `CharacterEncodingFilter`：字元編碼過濾器
- `RequestContextFilter`：請求上下文過濾器

### Liberty 的支援狀況
IBM Liberty 透過以下功能支援無 web.xml 部署：
- **Annotation Scanning**：自動掃描註解
- **ServletContainerInitializer**：Spring Boot 的初始化器
- **Fragment JAR 支援**：從依賴 JAR 中讀取配置

## repackage 會做的事情?

### 1. 創建可執行 JAR/WAR
- **標準 WAR**：傳統的 Web 應用程式格式，需要部署到應用程式伺服器
- **可執行 WAR**：包含內嵌 Servlet 容器（如 Tomcat），可以直接執行

### 2. 依賴項重新組織
```
標準 WAR 結構：
├── WEB-INF/
│   ├── classes/          # 應用程式類別檔案
│   ├── lib/              # 依賴項 JAR 檔案
│   └── web.xml (可選)
└── 靜態資源檔案

可執行 WAR 結構：
├── WEB-INF/
│   ├── classes/          # 應用程式類別檔案
│   ├── lib/              # 應用程式依賴項
│   ├── lib-provided/     # 容器提供的依賴項
│   └── web.xml (可選)
├── org/springframework/boot/loader/  # Spring Boot Loader
└── 靜態資源檔案
```

### 3. 添加 Boot Loader
- 添加 `org.springframework.boot.loader.launch.WarLauncher`
- 修改 MANIFEST.MF 檔案，設定 Main-Class
- 使應用程式可以透過 `java -jar app.war` 執行

## 停用 repackage 的影響

### ✅ 優點（對 Liberty 部署）
1. **檔案結構標準化**：使用標準 WAR 結構，符合 Java EE 規範
2. **檔案大小減少**：移除 Spring Boot Loader，減少約 300KB
3. **相容性更好**：與傳統應用程式伺服器相容性更佳
4. **避免 lib-provided**：不會產生 lib-provided 資料夾

### ❌ 缺點
1. **不可直接執行**：無法使用 `java -jar` 執行
2. **失去內嵌容器優勢**：無法獨立運行
3. **部署依賴**：必須部署到外部應用程式伺服器

## 實際比較

### 標準 WAR（停用 repackage）
```bash
# 無法直接執行
java -jar mq-monitor-0.0.1-SNAPSHOT.war  # ❌ 錯誤

# 必須部署到 Liberty
liberty/bin/server start myServer
# 部署 WAR 到 dropins/ 或透過 server.xml 配置
```

### 可執行 WAR（啟用 repackage）
```bash
# 可以直接執行（包含內嵌 Tomcat）
java -jar mq-monitor-0.0.1-SNAPSHOT.war  # ✅ 可執行

# 也可以部署到 Liberty（但會有 lib-provided 問題）
```

## 對您專案的影響評估

### 目前配置（停用 repackage）
- ✅ 完全適合 Liberty 部署
- ✅ 沒有 lib-provided 問題
- ✅ WAR 檔案最小化
- ❌ 失去開發時快速啟動能力

### 建議的最佳實踐
```xml
<!-- 開發環境：啟用 repackage，方便本地測試 -->
<profile>
    <id>dev</id>
    <!-- Spring Boot Plugin 使用預設配置，支援 java -jar -->
</profile>

<!-- 生產環境：停用 repackage，標準 WAR 部署 -->
<profile>
    <id>liberty</id>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <skip>true</skip>  <!-- 停用 repackage -->
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>
```
