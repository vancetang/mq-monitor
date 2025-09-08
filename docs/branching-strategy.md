# 📌 Git 分支命名規則與策略

## 1. 主分支 (Main Branches)
- **`main`**  
  - 永遠保持可發佈的狀態 (Production-ready)。  
  - 所有正式版本 (Tag) 基於 `main`。  
- **`develop`**（可選，適合多人協作的大型專案）  
  - 作為新功能的整合分支。  
  - 所有功能、修復等分支合併後，才進入 `develop`。  
  - 小型專案可直接從 `main` 開發。

---

## 2. 分支類型與命名規則

### 功能分支 (Feature Branches)
- **命名格式：** `feat/<描述>`  
- 用途：開發新功能或需求。  
- 範例：
  - `feat/login-api`
  - `feat/ui-dark-mode`

---

### 錯誤修復分支 (Fix Branches)
- **命名格式：** `fix/<描述>`  
- 用途：修正 bug。  
- 範例：
  - `fix/nullpointer-user-service`
  - `fix/mq-connection-timeout`

---

### 維護分支 (Chore Branches)
- **命名格式：** `chore/<描述>`  
- 用途：程式碼維護、依賴升級、CI/CD 設定、格式化、文件更新。  
- 範例：
  - `chore/update-maven-plugin`
  - `chore/add-readme`

---

### 熱修分支 (Hotfix Branches)
- **命名格式：** `hotfix/<描述>`  
- 用途：生產環境的緊急修復，直接從 `main` 切出。  
- 範例：
  - `hotfix/fix-auth-token-expiry`

---

### 發佈分支 (Release Branches)
- **命名格式：** `release/<版本號>`  
- 用途：準備發佈版本，進行測試與最後修正。  
- 範例：
  - `release/1.0.0`
  - `release/2.1.0`

---

### 實驗分支 (Experiment Branches，可選)
- **命名格式：** `exp/<描述>`  
- 用途：測試新技術或快速驗證 PoC，不一定要合併。  
- 範例：
  - `exp/db2-index-optimization`
  - `exp/ui-prototype-v2`

---

### 文件分支 (Docs Branches，可選)
- **命名格式：** `docs/<描述>`  
- 用途：專門處理文件或規格相關變更。  
- 範例：
  - `docs/add-api-spec`
  - `docs/update-uml-diagram`

---

## 3. 規範要點
1. **小寫英文字母**，單字用 **`-`** 分隔。  
2. 分支名稱應簡潔、明確，避免過長。  
3. 與 **Conventional Commits** 保持一致 (`feat`, `fix`, `chore`, `docs`, …)。  
4. 建議設定 **分支保護規則**：  
   - 禁止直接推送 `main`  
   - 所有合併須經 Pull Request  
   - 必要時強制 Code Review 與 CI 測試通過  
