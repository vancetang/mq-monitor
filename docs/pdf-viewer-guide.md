# PDF 檢視器模組說明文件 (PDF.js Guide)

本文件說明專案中使用的 PDF.js 版本差異、架構調整原因及開發注意事項。本專案目前採取 **v3 (Legacy)** 與 **v5 (Modern)** 版本並存的策略，以滿足不同的部署與使用場景。

## 1. 版本架構差異

PDF.js 自 v4 版本起進行了重大的架構重構，全面轉向現代化的模組標準。以下為本專案中使用的兩個版本之關鍵差異：

| 特性             | **v3.11.174 (Legacy)**                | **v5.4.530 (Modern)**                          |
| :--------------- | :------------------------------------ | :--------------------------------------------- |
| **模組規範**     | **UMD** (Universal Module Definition) | **ESM** (ECMAScript Modules)                   |
| **檔案副檔名**   | `.js` (如 `pdf.min.js`)               | `.mjs` (如 `pdf.mjs`)                          |
| **執行環境要求** | **寬鬆**：支援 `file://` 協定         | **嚴格**：必須透過 **HTTP Server** (`http://`) |
| **部署方式**     | 可直接雙擊 HTML 檔案開啟              | 必須部署於 Web Server (如 Spring Boot, Nginx)  |
| **瀏覽器支援**   | 兼容性較高，支援較舊瀏覽器            | 僅支援現代瀏覽器 (需支援 Top-level await)      |
| **CORS 限制**    | 較少受限                              | **嚴格受限** (因 Module Loading 安全策略)      |

### 為什麼 v5 不能直接點開？
ESM 模組 (`import ... from ...`) 運作時，瀏覽器會發起網路請求來載入相依檔案。基於安全性考量 (CORS 策略)，現代瀏覽器**禁止**網頁透過 `file://` 協定發起跨來源請求，且 Web Worker 必須遵守同源政策 (Same-Origin Policy)。因此，v5 版本必須在 HTTP 伺服器環境下才能正常運作。

## 2. 專案目錄結構

為了區隔不同版本的資源與用途，專案採用以下結構：

```
C:\workspace-vscode\mq-monitor\
├── local-pdf-viewer.html          # [v3] 本地預覽入口 (雙擊即用)
├── pdfjs-assets-v3.11.174/        # [v3] UMD 靜態資源 (含 .js, .css)
│
└── src/
    └── main/
        └── resources/
            └── static/
                ├── local-pdf-viewer-v5.html    # [v5] 伺服器版入口
                └── pdfjs-assets-v5.4.530/      # [v5] ESM 模組資源
                    ├── images/                 # SVG 圖示資源
                    ├── pdf.mjs
                    ├── pdf.worker.mjs
                    ├── pdf_viewer.mjs
                    └── pdf_viewer.css
```

## 3. 使用指南

### 🅰️ 場景 A：單機離線使用 (無需 Server)
若需要將 HTML 檔案發送給使用者，讓其直接雙擊開啟，**請使用 v3 版本**。

*   **入口檔案**：`local-pdf-viewer.html`
*   **特性**：不依賴任何伺服器環境，完全離線可用。

### 🅱️ 場景 B：整合至 Web 系統 (Spring Boot)
若是要整合進本專案的 Web 介面中，**建議使用 v5 版本** 以獲得更好的效能與長期支援。

*   **入口檔案**：`src/main/resources/static/local-pdf-viewer-v5.html`
*   **啟動方式**：
    1.  啟動 Spring Boot 應用程式。
    2.  瀏覽器存取：`http://localhost:8080/local-pdf-viewer-v5.html`

### 🧪 開發測試 v5
若要在不啟動完整 Spring Boot 應用的情況下測試 v5 頁面，可使用 Node.js 快速啟動靜態伺服器：

```powershell
# 在專案根目錄執行 (需安裝 Node.js)
npx http-server src/main/resources/static -o local-pdf-viewer-v5.html
```

## 4. 維護與升級

*   **v3 (3.11.174)**：這是最後一個支援 UMD 的版本。除非有重大安全性漏洞，否則**不應再升級此版本**，以免破壞「雙擊開啟」的功能。
*   **v5 (5.x+)**：可隨時跟隨官方最新版本升級。升級時請確保下載完整的 ESM 構建檔案 (包含 `images` 資料夾)，並更新 HTML 中的 `import` 路徑。

## 5. 常見問題 (FAQ)

**Q: 為什麼 v5 的 HTML 裡面要寫 `<script type="module">`？**
A: 這是告訴瀏覽器這段程式碼使用 ESM 標準，瀏覽器才會正確處理 `import` 語法。

**Q: 為什麼 v5 需要 `images` 資料夾？**
A: v5 的 CSS (`pdf_viewer.css`) 改用外部 SVG 檔案來顯示工具列圖示，以提供更高解析度的介面，不再將圖示內嵌於 CSS 中。

**Q: 瀏覽器 Console 出現 `Access to script ... blocked by CORS policy`？**
A: 這代表您嘗試直接雙擊開啟 v5 的 HTML 檔案。請改用 HTTP Server 方式開啟，或改用 v3 版本。
