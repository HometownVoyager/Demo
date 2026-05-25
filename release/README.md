# DeepSeek Chat Client v1.0.0

## 下载

- **可执行 JAR 文件**: `deepseek-chat-client.jar`

## 运行方式

### 方法 1: 直接运行（需要 Java 环境）

```bash
java -jar deepseek-chat-client.jar
```

### 方法 2: 使用提供的脚本

```bash
./run_demo.sh
```

## 系统要求

- Java 11 或更高版本
- 图形界面环境（X11、Windows 或 macOS）

## 功能特性

- 🎨 现代化 UI 设计，采用渐变配色方案
- 💬 与 DeepSeek AI 进行聊天对话
- 📝 角色卡管理功能
- 📚 世界观设定支持
- ⚙️ API Key 配置管理
- 💾 本地数据持久化存储

## 使用说明

1. 首次运行时，需要在设置面板中配置您的 DeepSeek API Key
2. 可以选择和创建不同的角色卡来定制 AI 的回复风格
3. 支持创建和管理世界观设定，为对话提供背景上下文
4. 聊天记录会自动保存

## 构建说明

如果您想自己构建项目：

```bash
cd deepseek-chat-restructure
mvn clean package
```

构建完成后，可执行 JAR 文件位于 `chat-ui/target/deepseek-chat-client.jar`

## 许可证

本项目仅供学习和研究使用。
