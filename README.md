# Spring AI Alibaba 脚手架

基于 **Spring AI Alibaba 1.1.2.1** + **Java 21** + **Spring Boot 3.4.5** 的快速启动脚手架。

## 技术栈

| 组件 | 版本 |
|------|------|
| Java | 21 |
| Spring Boot | 3.4.5 |
| Spring AI Alibaba | 1.1.2.1 |
| Spring AI | 1.1.2（BOM 自动对齐） |
| 模型服务 | 阿里云 DashScope（通义千问） |

## 快速开始

### 1. 获取 API Key

前往 [阿里云百炼控制台](https://bailian.console.aliyun.com/) 创建 API Key。

### 2. 配置环境变量

```bash
export DASHSCOPE_API_KEY=sk-xxxxxxxxxxxx
```

或直接修改 `src/main/resources/application.yml` 中的 `api-key`。

### 3. 启动项目

```bash
mvn spring-boot:run
```

## 接口列表

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/ai/chat?message=xxx` | 同步对话 |
| GET | `/ai/chat/stream?message=xxx` | 流式对话（SSE） |
| POST | `/ai/chat/with-system` | 带系统提示词对话 |
| GET | `/ai/tool/chat?message=xxx` | Function Calling 示例 |
| GET | `/actuator/health` | 健康检查 |

### 请求示例

**同步对话：**
```bash
curl "http://localhost:8080/ai/chat?message=你好"
```

**流式对话：**
```bash
curl -N "http://localhost:8080/ai/chat/stream?message=介绍一下Spring AI"
```

**带系统提示词：**
```bash
curl -X POST http://localhost:8080/ai/chat/with-system \
  -H "Content-Type: application/json" \
  -d '{"systemPrompt":"你是一名资深 Java 工程师","userMessage":"介绍一下虚拟线程"}'
```

**Function Calling：**
```bash
curl "http://localhost:8080/ai/tool/chat?message=现在几点了"
```

## 项目结构

```
src/main/java/com/example/demo/
├── DemoApplication.java          # 启动类
├── config/
│   ├── ChatClientConfig.java     # ChatClient Bean 配置
│   └── ToolConfig.java           # Function Calling 工具注册
├── controller/
│   ├── ChatController.java       # 对话接口
│   └── ToolChatController.java   # 工具调用接口
└── service/
    └── ChatService.java          # 对话业务逻辑
```

## 扩展指引

- **多模态**：替换为 `qwen-vl-max` 模型，传入图片 URL 即可。
- **RAG 向量检索**：引入 `spring-ai-alibaba-starter-dashscope`（内含 Embedding）+ 向量数据库 Starter。
- **多 Agent**：使用 `spring-ai-alibaba-agent-framework` 中的 `SequentialAgent` / `ParallelAgent`。
- **MCP 集成**：引入 `spring-ai-alibaba-starter-nacos-mcp-server` 对接 Nacos MCP。
