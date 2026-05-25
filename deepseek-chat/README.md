# DeepSeek Chat Client

一个简单易用的 DeepSeek AI 聊天客户端，具有图形界面。

## 功能特点

- ✅ 连接 DeepSeek API 进行对话
- ✅ 本地存储 API Key（保存在 `deepseek_config.properties`）
- ✅ 本地存储历史对话（保存在 `chat_history.txt`）
- ✅ 自定义 System Prompt
- ✅ 图形界面，傻瓜式操作
- ✅ 自动保存对话历史

## 使用方法

### 1. 编译程序

```bash
cd deepseek-chat
javac -d out src/DeepSeekChatClient.java
```

### 2. 运行程序

```bash
./run.sh
```

或者：

```bash
java -cp out DeepSeekChatClient
```

### 3. 使用步骤

1. **输入 API Key**: 在设置区域的 "API Key" 输入框中输入你的 DeepSeek API Key
2. **设置 System Prompt** (可选): 可以自定义 AI 的行为和角色，默认为 "You are a helpful assistant."
3. **保存设置**: 点击 "保存设置" 按钮，API Key 和 System Prompt 会被保存到本地文件
4. **开始对话**: 在底部输入框输入消息，按回车或点击 "发送" 按钮
5. **清空对话**: 点击 "清空对话" 按钮可以清除所有聊天记录

## 文件说明

- `src/DeepSeekChatClient.java` - 源代码
- `out/` - 编译后的 class 文件
- `run.sh` - 运行脚本
- `deepseek_config.properties` - 配置文件（运行后生成，存储 API Key 和 System Prompt）
- `chat_history.txt` - 对话历史文件（运行后生成）

## 获取 DeepSeek API Key

1. 访问 https://platform.deepseek.com/
2. 注册/登录账号
3. 在 API Keys 页面创建新的 API Key
4. 复制 API Key 到程序中

## 系统要求

- Java 17 或更高版本
- 网络连接（用于访问 DeepSeek API）

## 注意事项

- API Key 会以明文形式存储在本地配置文件中，请妥善保管
- 对话历史会保存在本地，关闭程序时自动保存
- 每次发送请求都会包含完整的对话历史，以便 AI 理解上下文
