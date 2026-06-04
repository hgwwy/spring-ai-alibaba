# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

这个仓库是一个 Maven 聚合工程，用来承载 Spring AI Alibaba demo。根目录的 [pom.xml](pom.xml) 是 `packaging=pom` 的聚合 POM，目前只包含一个子模块：[spring-ai-agent](spring-ai-agent/pom.xml)。

当前模块的目标方向很明确：基于 Spring Boot 3.5、Spring AI 1.1.2、Spring AI Alibaba 1.1.2.2，围绕 DashScope、Multi-Agent、RAG、MCP 做演示型应用。但目前代码仍然接近脚手架状态，真实源码只有启动类、极简配置和一个 `contextLoads` 测试；README 描述的能力范围明显大于当前实现。

## 常用命令

默认在仓库根目录执行。

### 构建与测试

- 编译全部模块：
  - `mvn compile`
- 运行全部测试：
  - `mvn test`
- 仅运行应用模块测试：
  - `mvn -pl spring-ai-agent test`
- 运行单个测试类：
  - `mvn -pl spring-ai-agent -Dtest=SpringAiAgentApplicationTests test`
- 运行单个测试方法：
  - `mvn -pl spring-ai-agent -Dtest=SpringAiAgentApplicationTests#contextLoads test`
- 打包应用模块：
  - `mvn -pl spring-ai-agent package`

### 启动应用

- 启动 Spring Boot 应用：
  - `mvn -pl spring-ai-agent spring-boot:run`

README 假设 DashScope 通过环境变量 `DASHSCOPE_API_KEY` 提供密钥，但当前 [application.yaml](spring-ai-agent/src/main/resources/application.yaml) 还没有把这部分配置落地。

### 依赖与 API 排查

这个仓库经常需要确认 Spring AI / Spring AI Alibaba 的实际 API，因此下面这些命令很有用：

- 解析模块依赖：
  - `mvn -pl spring-ai-agent dependency:resolve`
- 生成 classpath 文件，便于本地 `javap` / API 排查：
  - `mvn -pl spring-ai-agent dependency:build-classpath -Dmdep.outputFile=target/classpath.txt`
- 查看应用模块依赖树：
  - `mvn -pl spring-ai-agent dependency:tree`

## 架构理解

### 仓库结构

- 根 [pom.xml](pom.xml)
  - 统一管理 Java、Spring Boot、Spring AI、Spring AI Alibaba 版本
  - 通过 BOM 导入 Spring AI 与 Spring AI Alibaba 依赖版本
  - 定义唯一子模块 `spring-ai-agent`
- [spring-ai-agent/pom.xml](spring-ai-agent/pom.xml)
  - 真正的应用模块
  - 当前已经引入 Web、Validation、Actuator、Spring AI ChatClient、DashScope、Agent Framework、RAG、MCP Router、Elasticsearch Vector Store 等依赖

### 当前代码状态

当前实际源码非常少：

- [SpringAiAgentApplication.java](spring-ai-agent/src/main/java/com/alibaba/ai/agent/SpringAiAgentApplication.java)
  - 只有 `@SpringBootApplication` 启动入口
- [application.yaml](spring-ai-agent/src/main/resources/application.yaml)
  - 目前只配置了 `spring.application.name`
- [SpringAiAgentApplicationTests.java](spring-ai-agent/src/test/java/com/alibaba/ai/agent/SpringAiAgentApplicationTests.java)
  - 只有一个 Spring 上下文加载测试

也就是说，后续绝大多数功能开发都会发生在 `spring-ai-agent` 模块内，而且基本属于从零补齐结构。

### 依赖反映出的目标架构

虽然源码还没展开，但 [spring-ai-agent/pom.xml](spring-ai-agent/pom.xml) 已经把目标方向表达得很清楚：

- `org.springframework.ai:spring-ai-client-chat`
- `org.springframework.ai:spring-ai-autoconfigure-model-chat-client`
  - 说明项目预期使用 `ChatClient` 作为模型调用入口
- `com.alibaba.cloud.ai:spring-ai-alibaba-starter-dashscope`
  - 提供 DashScope 的 Chat / Embedding 模型接入
- `com.alibaba.cloud.ai:spring-ai-alibaba-agent-framework`
  - 预期使用 `ReactAgent`、`SequentialAgent`、`ParallelAgent` 等多 Agent 编排能力
- `com.alibaba.cloud.ai:spring-ai-alibaba-starter-rag`
  - 预期接入 RAG 流程
- `org.springframework.ai:spring-ai-starter-vector-store-elasticsearch`
  - 预期用 Elasticsearch 做向量存储
- `com.alibaba.cloud.ai:spring-ai-alibaba-starter-mcp-router`
  - 预期接入 MCP Router / 工具路由能力

因此，后续实现大概率会自然分成以下几层：

- `controller`：HTTP 接口层
- `config`：模型、Agent、RAG、MCP、工具相关 Bean 配置
- `service`：业务编排和工作流逻辑
- `tool` / `rag` / `agent`：工具定义、知识库接入、Agent 装配

这些包结构目前还不存在，但这是理解这个仓库时最重要的“大图景”。

### README 与真实代码存在明显偏差

[README.md](README.md) 目前更像“目标说明”而不是“现状说明”：

- README 里已经列出了 `/ai/chat`、`/ai/chat/stream`、`/ai/chat/with-system`、`/ai/tool/chat` 等接口
- README 还提到了 `SequentialAgent / ParallelAgent`、RAG、MCP 集成
- 这些接口和对应实现类目前在源码中都还不存在
- README 里的版本写的是 Spring Boot `3.4.5`、Spring AI Alibaba `1.1.2.1`，但实际根 [pom.xml](pom.xml) 使用的是 Spring Boot `3.5.7`、Spring AI Alibaba `1.1.2.2`

后续改代码时，要把 README 当成“目标产品方向”，不要把它当成“当前实现说明”。如果实现推进了，README 需要同步修正。

## 后续修改时的注意点

- 处理应用代码时，优先使用带 `-pl spring-ai-agent` 的 Maven 命令，因为根工程只是聚合层。
- 如果增加 AI 接口、Agent 编排、RAG 或 MCP 相关实现，记得同步更新 README；现在 README 明显领先于源码。
- 这个仓库很适合通过 `dependency:build-classpath` 配合 `javap` 做本地 API 核对，特别是在确认 Spring AI Alibaba Agent Framework 这类依赖的真实可用方法时。