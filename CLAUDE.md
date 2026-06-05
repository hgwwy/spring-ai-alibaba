# CLAUDE.md

## 前置条件
1. 必须请始终用中文回复。
2. 强制只有在plan mode 或是 有明确指令要求做计划的情况下才使用superpowers插件

## 项目概览

这个仓库是一个 Maven 聚合工程，用来承载 Spring AI Alibaba demo。根目录的 [pom.xml](pom.xml) 是 `packaging=pom` 的聚合 POM，目前只包含一个子模块：[spring-ai-agent](spring-ai-agent/pom.xml)。

当前模块的目标方向很明确：基于 Spring Boot 3.5、Spring AI 1.1.2、Spring AI Alibaba 1.1.2.2，围绕 DashScope、Multi-Agent、RAG、MCP 做演示型应用。现在仓库已经不再是纯脚手架状态，`spring-ai-agent` 模块中已经落了一版可运行的多 Agent 旅行规划示例，但 RAG、MCP、向量检索仍然只停留在依赖层，尚未进入业务实现。

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
- 跳过测试打包应用模块：
  - `mvn -pl spring-ai-agent -DskipTests package`

### 启动应用

- 启动 Spring Boot 应用：
  - `mvn -pl spring-ai-agent spring-boot:run`

当前 [application.yaml](spring-ai-agent/src/main/resources/application.yaml) 已经补充了 `spring.ai.dashscope.api-key` 与 `qwen-plus` 模型配置，同时提供了 `demo.multi-agent.mock-enabled` 开关。默认值为 `true`，也就是默认走 mock 模式，不依赖真实 DashScope。

如果要切到真实模型模式，至少需要：

- 设置环境变量 `DASHSCOPE_API_KEY`
- 将 `demo.multi-agent.mock-enabled` 改为 `false`

### 依赖与 API 排查

这个仓库经常需要确认 Spring AI / Spring AI Alibaba 的实际 API，因此下面这些命令很有用：

- 解析模块依赖：
  - `mvn -pl spring-ai-agent dependency:resolve`
- 生成 classpath 文件，便于本地 `javap` / API 排查：
  - `mvn -pl spring-ai-agent dependency:build-classpath -Dmdep.outputFile=target/classpath.txt`
- 查看应用模块依赖树：
  - `mvn -pl spring-ai-agent dependency:tree`
- 查看 Agent Framework / ChatClient 的真实签名：
  - `javap -classpath "$(cat spring-ai-agent/target/classpath.txt)" com.alibaba.cloud.ai.graph.agent.ReactAgent`
  - `javap -classpath "$(cat spring-ai-agent/target/classpath.txt)" com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent`
  - `javap -classpath "$(cat spring-ai-agent/target/classpath.txt)" com.alibaba.cloud.ai.graph.agent.flow.agent.ParallelAgent`
  - `javap -classpath "$(cat spring-ai-agent/target/classpath.txt)" org.springframework.ai.chat.client.ChatClient`

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

当前实际源码已经有了一版最小多 Agent 示例，主要集中在 `spring-ai-agent` 模块：

- [SpringAiAgentApplication.java](spring-ai-agent/src/main/java/com/alibaba/ai/agent/SpringAiAgentApplication.java)
  - Spring Boot 启动入口，并启用了 `AgentProperties` 配置绑定
- [application.yaml](spring-ai-agent/src/main/resources/application.yaml)
  - 已配置 `spring.ai.dashscope.api-key`、`qwen-plus` 模型和 `demo.multi-agent.*` 自定义配置
- `controller`
  - [MultiAgentDemoController.java](spring-ai-agent/src/main/java/com/alibaba/ai/agent/controller/MultiAgentDemoController.java)
  - 暴露 `POST /api/agents/travel-plan` 接口
- `service`
  - [MultiAgentDemoService.java](spring-ai-agent/src/main/java/com/alibaba/ai/agent/service/MultiAgentDemoService.java)
  - 编排交通专家、住宿专家、协调专家和本地工具，生成结构化响应
- `agent`
  - [FlightPlanningAgent.java](spring-ai-agent/src/main/java/com/alibaba/ai/agent/agent/FlightPlanningAgent.java)
  - [HotelPlanningAgent.java](spring-ai-agent/src/main/java/com/alibaba/ai/agent/agent/HotelPlanningAgent.java)
  - [TravelCoordinatorAgent.java](spring-ai-agent/src/main/java/com/alibaba/ai/agent/agent/TravelCoordinatorAgent.java)
  - [SpecialistAgent.java](spring-ai-agent/src/main/java/com/alibaba/ai/agent/agent/SpecialistAgent.java)
- `tool`
  - [BudgetTool.java](spring-ai-agent/src/main/java/com/alibaba/ai/agent/tool/BudgetTool.java)
  - [CalendarTool.java](spring-ai-agent/src/main/java/com/alibaba/ai/agent/tool/CalendarTool.java)
- `config`
  - [AgentProperties.java](spring-ai-agent/src/main/java/com/alibaba/ai/agent/config/AgentProperties.java)
  - [ChatClientConfig.java](spring-ai-agent/src/main/java/com/alibaba/ai/agent/config/ChatClientConfig.java)
  - [MultiAgentConfig.java](spring-ai-agent/src/main/java/com/alibaba/ai/agent/config/MultiAgentConfig.java)
  - [ToolConfig.java](spring-ai-agent/src/main/java/com/alibaba/ai/agent/config/ToolConfig.java)
- `model`
  - [TravelPlanRequest.java](spring-ai-agent/src/main/java/com/alibaba/ai/agent/model/TravelPlanRequest.java)
  - [TravelPlanResponse.java](spring-ai-agent/src/main/java/com/alibaba/ai/agent/model/TravelPlanResponse.java)
  - [AgentPlanContext.java](spring-ai-agent/src/main/java/com/alibaba/ai/agent/model/AgentPlanContext.java)
  - [AgentResult.java](spring-ai-agent/src/main/java/com/alibaba/ai/agent/model/AgentResult.java)

测试也已经不是只有 `contextLoads`，当前至少包括：

- [SpringAiAgentApplicationTests.java](spring-ai-agent/src/test/java/com/alibaba/ai/agent/SpringAiAgentApplicationTests.java)
- [MultiAgentIntegrationTests.java](spring-ai-agent/src/test/java/com/alibaba/ai/agent/MultiAgentIntegrationTests.java)
- [MultiAgentDemoServiceTests.java](spring-ai-agent/src/test/java/com/alibaba/ai/agent/service/MultiAgentDemoServiceTests.java)
- [MultiAgentDemoControllerTests.java](spring-ai-agent/src/test/java/com/alibaba/ai/agent/controller/MultiAgentDemoControllerTests.java)
- [BudgetToolTests.java](spring-ai-agent/src/test/java/com/alibaba/ai/agent/tool/BudgetToolTests.java)
- [CalendarToolTests.java](spring-ai-agent/src/test/java/com/alibaba/ai/agent/tool/CalendarToolTests.java)

### 当前多 Agent 示例的真实边界

虽然已经落了多 Agent 示例，但要明确这是一版“演示骨架”，不是完全框架化编排：

- 目前业务编排主要由 `MultiAgentDemoService` 直接串联多个专家 Agent 和工具
- `FlightPlanningAgent`、`HotelPlanningAgent`、`TravelCoordinatorAgent` 在真实模型模式下通过 `ChatClient` 调用大模型
- 默认 `mock-enabled=true` 时不依赖真实 DashScope，会直接返回内置示例结果
- 目前还没有把 `ReactAgent`、`SequentialAgent`、`ParallelAgent` 真正接入业务流
- RAG、MCP、Elasticsearch Vector Store 依赖已经存在，但还没有对应业务实现

因此，后续如果用户要求“真正基于 Spring AI Alibaba Agent Framework 做多 Agent 编排”，应理解为要把当前 service 级编排进一步下沉到框架级 Agent/Flow Agent 实现中。

### 依赖反映出的目标架构

虽然当前实现还比较轻量，但 [spring-ai-agent/pom.xml](spring-ai-agent/pom.xml) 已经把目标方向表达得很清楚：

- `org.springframework.ai:spring-ai-client-chat`
- `org.springframework.ai:spring-ai-autoconfigure-model-chat-client`
  - 当前已实际用于 `ChatClient` 驱动的专家 Agent 调用
- `com.alibaba.cloud.ai:spring-ai-alibaba-starter-dashscope`
  - 提供 DashScope 的 Chat / Embedding 模型接入
- `com.alibaba.cloud.ai:spring-ai-alibaba-agent-framework`
  - 后续可以演进到 `ReactAgent`、`SequentialAgent`、`ParallelAgent` 等真正的多 Agent 编排
- `com.alibaba.cloud.ai:spring-ai-alibaba-starter-rag`
  - 预期后续接入 RAG 流程
- `org.springframework.ai:spring-ai-starter-vector-store-elasticsearch`
  - 预期后续用 Elasticsearch 做向量存储
- `com.alibaba.cloud.ai:spring-ai-alibaba-starter-mcp-router`
  - 预期后续接入 MCP Router / 工具路由能力

从现在的代码和依赖组合看，后续实现自然会分成以下几层：

- `controller`：HTTP 接口层
- `service`：业务编排和工作流逻辑
- `agent`：专家 Agent、协调 Agent、后续的框架级 Agent 封装
- `tool`：本地工具定义
- `config`：模型、Agent、工具相关 Bean 配置
- `model`：请求、响应与 Agent 上下文模型
- 后续可再扩展 `rag` / `mcp`

## 开发约束

- 新增或修改 Java / 配置 / 测试文件时，必须补充中文注释，且注释要明确表达职责、输入输出或设计意图，不能只写空泛描述。
- 没有中文注释的新增文件，视为不符合本仓库规范。
- 修改已有文件时，如果发现该文件中的类、关键字段、方法缺少中文注释，应一并补齐，不要只改业务代码不补文档性说明。
- 至少保证这些层级存在中文注释：
  - 类 / 接口 / record 注释
  - 关键字段注释（尤其是 service、agent、config 中的依赖字段和配置字段）
  - 方法注释（尤其是公开方法、Bean 注册方法、编排入口方法、测试方法）
- 如果一个文件已经存在注释风格，后续修改时要保持一致，不要混用英文注释或无意义注释。
- Prompt、工具描述、接口说明、测试断言中的展示性文案，默认保持中文，除非外部 API 明确要求英文。

## 后续修改时的注意点

- 处理应用代码时，优先使用带 `-pl spring-ai-agent` 的 Maven 命令，因为根工程只是聚合层。
- 当前默认 `mock-enabled=true`，这使得本地开发和测试不依赖真实 DashScope；如果你要改成真实调用，记得同步考虑测试隔离。
- 现在的集成测试通过给 DashScope 注入测试 key，并排除了 Elasticsearch Vector Store / Alibaba RAG 的自动配置，避免测试时去连外部服务。新增 `@SpringBootTest` 时，优先复用这个模式。
- 如果增加真正的 RAG、MCP、Elasticsearch 功能，记得重新审视测试启动路径，否则很容易在测试上下文加载阶段触发外部依赖连接。
- 这个仓库很适合通过 `dependency:build-classpath` 配合 `javap` 做本地 API 核对，特别是在确认 Spring AI Alibaba Agent Framework 的真实可用方法时。
- `spring-ai-alibaba` 1.1.2.2 版本可能不在公共 Maven 仓库中（pom.xml 注释中已提示 "Install Spring AI Alibaba in your local"）。如果依赖解析失败，需要先在本地安装该版本。
- 仓库中目前没有 README.md 文件，但 CLAUDE.md 多处引用了它；如需参考 README 中描述的目标接口和功能，以本 CLAUDE.md 的描述为准。
- 当前代码中的注释、提示词和工具描述已经统一改成中文；新增示例代码时，默认保持中文文案风格。