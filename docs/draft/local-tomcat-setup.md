# 本地端Tomcat JNDI設定指南

## 1. 下載並設定Tomcat

### 下載Tomcat 10.x
```powershell
# 下載Tomcat 10.1.44 (與專案使用的版本一致)
# 解壓到 C:\apache-tomcat-10.1.44
```

### 設定環境變數
```powershell
$env:CATALINA_HOME="C:\apache-tomcat-10.1.44"
$env:CATALINA_BASE="C:\apache-tomcat-10.1.44"
```

## 2. 配置JNDI DataSource

### 步驟1：放置DB2 JDBC Driver
將DB2 JDBC Driver複製到Tomcat的lib目錄：
```powershell
# 從Maven倉庫複製
Copy-Item "$env:USERPROFILE\.m2\repository\com\ibm\db2\jcc\12.1.2.0\jcc-12.1.2.0.jar" "$env:CATALINA_HOME\lib\"
```

### 步驟2：修改 server.xml
編輯 `$CATALINA_HOME\conf\server.xml`，在 `<GlobalNamingResources>` 區段加入：

```xml
<GlobalNamingResources>
  <!-- 其他現有配置... -->
  
  <!-- DB2 DataSource 配置 -->
  <Resource name="jndi/eltwdb" 
            auth="Container"
            type="javax.sql.DataSource" 
            driverClassName="com.ibm.db2.jcc.DB2Driver"
            url="jdbc:db2://127.0.0.1:50000/eltw"
            username="vance" 
            password="1qaz2wsx"
            maxTotal="50" 
            maxIdle="10" 
            maxWaitMillis="10000"
            validationQuery="SELECT 1 FROM SYSIBM.SYSDUMMY1"
            testOnBorrow="true"/>
</GlobalNamingResources>
```

### 步驟3：修改 context.xml
編輯 `$CATALINA_HOME\conf\context.xml`，加入ResourceLink：

```xml
<Context>
  <!-- 其他現有配置... -->
  
  <!-- 連結全域JNDI資源 -->
  <ResourceLink name="jndi/eltwdb" 
                global="jndi/eltwdb"
                type="javax.sql.DataSource"/>
</Context>
```

## 3. 部署與啟動

### 部署WAR檔案
```powershell
# 複製WAR檔案到webapps目錄
Copy-Item "target\mq-monitor-0.0.1-SNAPSHOT.war" "$env:CATALINA_HOME\webapps\"
```

### 啟動Tomcat（一般模式）
```powershell
cd $env:CATALINA_HOME\bin
.\startup.bat
```

### 啟動Tomcat（Debug模式）
```powershell
cd $env:CATALINA_HOME\bin
$env:JPDA_ADDRESS="*:8000"
$env:JPDA_TRANSPORT="dt_socket"
.\catalina.bat jpda start
```

### 設定Spring Profile
建立 `$CATALINA_HOME\bin\setenv.bat`：
```bat
set "CATALINA_OPTS=%CATALINA_OPTS% -Dspring.profiles.active=container"
```

## 4. 驗證設定

### 檢查JNDI資源
訪問 Tomcat Manager 或查看日誌確認JNDI資源是否正確載入。

### 測試應用程式
1. 啟動Tomcat後，訪問 `http://localhost:8080/mq-monitor-0.0.1-SNAPSHOT/`
2. 檢查應用程式是否能正確連接到資料庫

## 5. 常見問題排除

### 問題：ClassNotFoundException for DB2 Driver
**解決方案：** 確認DB2 JDBC Driver已正確放置在 `$CATALINA_HOME\lib\` 目錄

### 問題：JNDI Name not found
**解決方案：** 
1. 檢查server.xml中的Resource name是否正確
2. 檢查context.xml中的ResourceLink配置
3. 確認Spring Profile `container` 已啟用

### 問題：連接資料庫失敗
**解決方案：**
1. 檢查DB2資料庫是否正在運行
2. 驗證連接字串、用戶名、密碼是否正確
3. 檢查防火牆設定
