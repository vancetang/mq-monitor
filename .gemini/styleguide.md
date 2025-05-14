# Gemini 程式碼審查風格指南（適用於 Java + Spring Boot 專案）

## General Response Language
- **IMPORTANT:** Please provide all responses, comments, and suggestions exclusively in **Traditional Chinese (繁體中文)**.

## 語言規定
- 所有程式碼審查意見請以 **繁體中文** 撰寫。
- 請避免使用簡體字或英文審查內容（除非特定術語需要）。
- 回覆應清晰易懂、具建設性。

## 程式碼命名風格
- 類別名稱使用 PascalCase（例如：`UserService`, `OrderController`）。
- 變數與方法名稱使用 camelCase（例如：`userName`, `processOrder()`）。
- 常數名稱使用全大寫並以下底線分隔（例如：`MAX_RETRY_COUNT`）。
- Spring Bean 命名請與功能相關（例如：`userService`, `orderRepository`）。

## Spring Boot 特別規則
- Controller 類別需以 `@RestController` 註解，並以 `/api` 為根路由。
- Service 層邏輯需集中在 `@Service` 類別中，避免寫在 Controller。
- Repository 應使用 `JpaRepository` 或 `CrudRepository`，命名以 `Repository` 結尾。
- 使用 `@Transactional` 管理交易，而非手動 commit。

## 程式碼風格與格式
- 每行字元不超過 120 字。
- 使用 4 個空白字元做為縮排。
- 適當斷行與空行區分邏輯區塊。
- 若方法超過 50 行，應考慮拆分。
- Lambda 表達式建議保持簡潔，避免過度巢狀。

## 註解風格
- 所有公開方法應使用 Javadoc 標準格式，例如：

  ```java
  /**
   * 根據使用者 ID 取得訂單列表。
   *
   * @param userId 使用者 ID
   * @return 訂單清單
   */
  public List<Order> getOrdersByUserId(Long userId) { ... }
  ```

- 非公開邏輯可用 `//` 單行註解，簡潔扼要。

## 審查意見格式（回覆樣板）
- 若發現問題，請使用以下格式：

  ```
  ⚠️ 問題描述：
  這段程式碼違反了命名慣例，`user_name` 應改為 `userName`。

  ✅ 建議修正方式：
  將變數重新命名為 `userName`，符合 camelCase 命名風格。
  ```

- 建議盡量提出替代方案，協助改善品質而非單純指責。

## 其他
- 優先推薦使用 Spring 提供的功能（如 Validation、Security）而非自行實作重複邏輯。
- 請留意 NullPointerException 潛在風險，善用 `Optional` 或 null-safe 檢查。
