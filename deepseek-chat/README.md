# DeepSeek Chat Client

一个功能完整的 Java 图形界面聊天程序，支持连接 DeepSeek API 进行对话。

## 功能特点

### 基础功能
- ✅ 连接 DeepSeek API，实现 AI 对话
- ✅ 本地存储 API Key（`deepseek_config.properties`）
- ✅ 本地存储历史对话（`chat_history.txt`），启动时自动加载
- ✅ 自定义 System Prompt

### 新增功能
- ✅ **世界书管理** - 创建和管理世界观设定，让 AI 在特定背景下对话
- ✅ **角色卡管理** - 创建和管理角色卡片，让 AI 扮演特定角色
- ✅ 图形化管理界面，傻瓜式操作
- ✅ 下拉选择激活的世界书和角色卡

## 项目结构

```
deepseek-chat/
├── src/DeepSeekChatClient.java    # 源代码
├── out/                           # 编译后的 class 文件
├── run.sh                         # 运行脚本
└── README.md                      # 使用说明
```

## 使用方法

### 编译
```bash
cd deepseek-chat
javac -d out src/DeepSeekChatClient.java
```

### 运行
```bash
./run.sh
# 或
java -cp out DeepSeekChatClient
```

## 操作指南

### 1. 基本设置
1. 在"API Key"输入框中输入你的 DeepSeek API Key
2. （可选）在"System Prompt"文本区自定义 AI 的基础行为
3. 点击"保存设置"按钮

### 2. 使用世界书
1. 点击"管理世界书"按钮
2. 点击"新建"创建新的世界书
3. 输入名称和内容（如：世界观、背景设定、特殊规则等）
4. 点击"保存"
5. 在主界面的"世界书"下拉框中选择要启用的世界书

### 3. 使用角色卡
1. 点击"管理角色卡"按钮
2. 点击"新建"创建新的角色卡
3. 填写以下信息：
   - **名称**：角色名字
   - **描述**：角色外貌、身份等描述
   - **性格**：角色性格特点
   - **问候语**：角色开场白（可选）
4. 点击"保存"
5. 在主界面的"角色卡"下拉框中选择要扮演的角色

### 4. 开始对话
1. 在底部输入框输入消息
2. 按回车或点击"发送"按钮
3. AI 会根据你的设置（System Prompt + 世界书 + 角色卡）进行回复

## 数据存储

- **配置文件**：`deepseek_config.properties` - 存储 API Key 和 System Prompt
- **对话历史**：`chat_history.txt` - 存储所有对话记录
- **世界书**：`world_books.json` - 存储所有世界书数据
- **角色卡**：`characters.json` - 存储所有角色卡数据

## 技术实现

- 使用 `HttpURLConnection` 调用 DeepSeek API
- 使用 `Properties` 类管理配置文件
- 使用 `JTextPane` 显示带样式的聊天记录
- 异步线程处理 API 请求，避免界面卡顿
- 窗口关闭时自动保存对话历史

## 注意事项

1. 需要有效的 DeepSeek API Key 才能使用
2. 世界书和角色卡会合并到 System Prompt 中发送给 API
3. 建议合理控制世界书和角色卡的长度，避免超出 token 限制
