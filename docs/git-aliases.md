📌 Git Alias 建議

在 PowerShell 執行以下指令，加入 alias：

```powershell
git config --global alias.new-feat "!f() { git checkout -b feat/$1; }; f"
git config --global alias.new-fix "!f() { git checkout -b fix/$1; }; f"
git config --global alias.new-chore "!f() { git checkout -b chore/$1; }; f"
git config --global alias.new-hotfix "!f() { git checkout -b hotfix/$1; }; f"
git config --global alias.new-release "!f() { git checkout -b release/$1; }; f"
git config --global alias.new-docs "!f() { git checkout -b docs/$1; }; f"
git config --global alias.new-exp "!f() { git checkout -b exp/$1; }; f"
```

📌 使用方式

建立功能分支：

```powershell
git new-feat login-api
```

👉 建立並切換到 `feat/login-api`

建立修復分支：

```powershell
git new-fix mq-connection-timeout
```

👉 建立並切換到 `fix/mq-connection-timeout`

建立雜務分支：

```powershell
git new-chore update-maven-plugin
```

👉 建立並切換到 `chore/update-maven-plugin`

建立發佈分支：

```powershell
git new-release 1.0.0
```

👉 建立並切換到 `release/1.0.0`