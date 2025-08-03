# 程式碼品質與格式化指南

## 🎯 概述

本專案使用 **ktlint** 來確保 Kotlin 程式碼的一致性和品質。所有程式碼在推送到遠端儲存庫前都會自動檢查格式。

## 🔧 ktlint 配置

### 自動化檢查
- **Pre-push hook**: 推送前自動檢查格式
- **CI/CD**: GitHub Actions 自動執行檢查
- **IDE 整合**: 透過 EditorConfig 配置

### 格式化規則
- **縮排**: 4 空格
- **行長度**: 最大 120 字符
- **檔案結尾**: 必須有換行符
- **去除尾隨空格**: 自動清理

## 📋 常用命令

### 快速命令
```bash
# 檢查格式問題
./gradlew ktlintCheck

# 自動修復格式
./gradlew ktlintFormat

# 或使用便捷腳本
./scripts/check-format.sh check
./scripts/check-format.sh format
```

### Gradle 任務
```bash
# 檢查程式碼格式
./gradlew ktlintCheck

# 自動格式化程式碼
./gradlew ktlintFormat

# 執行所有檢查 (包含 ktlint)
./gradlew check

# 自定義格式化任務
./gradlew formatCode
```

## 🛠️ 設置步驟

### 1. 安裝 Pre-push Hook
```bash
# 自動安裝
./scripts/check-format.sh install

# 或手動複製
cp .git/hooks/pre-push.sample .git/hooks/pre-push
chmod +x .git/hooks/pre-push
```

### 2. IDE 配置

#### IntelliJ IDEA / Android Studio
1. 安裝 ktlint 插件
2. 設定 EditorConfig 支援
3. 配置自動格式化快捷鍵

```
Settings → Editor → Code Style → Kotlin
- 設定縮排為 4 空格
- 啟用 "Use tab character": 關閉
- 行長度限制: 120
```

#### VS Code
1. 安裝 Kotlin 擴充套件
2. 安裝 EditorConfig 擴充套件
3. 配置自動保存格式化

```json
// settings.json
{
    "editor.formatOnSave": true,
    "editor.insertSpaces": true,
    "editor.tabSize": 4
}
```

## 🚨 故障排除

### 常見問題

#### 1. Pre-push hook 不執行
```bash
# 檢查 hook 是否可執行
ls -la .git/hooks/pre-push

# 重新安裝
./scripts/check-format.sh install
```

#### 2. 格式檢查失敗
```bash
# 查看詳細錯誤
./gradlew ktlintCheck --info

# 自動修復
./gradlew ktlintFormat

# 檢查特定檔案
./gradlew ktlintCheck --continue
```

#### 3. CI 檢查失敗
```bash
# 本地重現 CI 環境
./gradlew clean ktlintCheck build

# 修復後重新推送
./gradlew ktlintFormat
git add .
git commit -m "Fix ktlint formatting issues"
git push
```

### 忽略特定檔案
在 `build.gradle.kts` 中配置：

```kotlin
ktlint {
    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
        exclude("**/*.gradle.kts")
    }
}
```

## 🔄 工作流程

### 開發流程
1. **編寫程式碼**
2. **本地檢查**: `./scripts/check-format.sh check`
3. **修復格式**: `./scripts/check-format.sh format`
4. **提交變更**: `git commit -m "Your message"`
5. **推送程式碼**: `git push origin main`

### 自動化流程
```
程式碼變更 → 本地檢查 → Pre-push Hook → 遠端推送 → CI 檢查 → 部署
```

## 📊 品質指標

### 檢查項目
- ✅ 縮排一致性
- ✅ 行尾空格清理
- ✅ 行長度限制
- ✅ 導入順序
- ✅ 空行規範
- ✅ 括號樣式

### 報告輸出
```
> Task :ktlintCheck
✅ 所有檢查通過
或
❌ 發現格式問題:
  src/main/kotlin/Example.kt:15:1: Unexpected indentation (indent)
  src/main/kotlin/Example.kt:20:121: Exceeded max line length (max-line-length)
```

## 🎨 EditorConfig 設定

專案根目錄的 `.editorconfig` 檔案：

```ini
root = true

[*.{kt,kts}]
charset = utf-8
end_of_line = lf
indent_style = space
indent_size = 4
insert_final_newline = true
trim_trailing_whitespace = true
max_line_length = 120
```

## 🚀 最佳實務

### 程式碼撰寫
1. **使用有意義的變數名稱**
2. **保持函數簡潔**
3. **適當的註解和文檔**
4. **遵循 Kotlin 慣例**

### 格式化建議
1. **推送前執行格式化**
2. **定期執行完整檢查**
3. **團隊成員使用相同設定**
4. **自動化整合到 IDE**

### 團隊協作
1. **共享 EditorConfig 設定**
2. **統一 IDE 配置**
3. **定期更新 ktlint 版本**
4. **文檔化特殊規則**

## 🔗 相關連結

- [ktlint 官方文檔](https://ktlint.github.io/)
- [Kotlin 編碼慣例](https://kotlinlang.org/docs/coding-conventions.html)
- [EditorConfig 規範](https://editorconfig.org/)
- [GitHub Actions 配置](https://docs.github.com/en/actions)