# 企业微信消息通知 SDK

基于 Java 17 + OkHttp + Jackson 实现的企业微信消息通知 SDK，支持两种消息推送方式：

1. **群机器人消息** — 通过 Webhook 向企业微信群发送消息
2. **应用消息（定向私信）** — 通过应用消息 API 向指定用户/部门/标签推送消息

## 功能特性

### 群机器人 (`WeComBotClient`)

| 消息类型 | 说明 |
|---------|------|
| 文本 `TextMessage` | 支持 @指定成员 / @所有人 |
| Markdown `MarkdownMessage` | 标题、引用、加粗、颜色、表格 |
| 图片 `ImageMessage` | 自动 Base64 编码 + MD5 |
| 图文 `NewsMessage` | 最多 8 条图文卡片 |
| 文件 `FileMessage` | 自动上传 + 发送 |
| 模板卡片 `TemplateCardMessage` | 文本通知型模板 |

### 应用消息 (`WeComAppClient`)

| 消息类型 | 说明 |
|---------|------|
| 文本 `AppTextMessage` | 定向推送文本 |
| Markdown `AppMarkdownMessage` | 定向推送 Markdown |
| 文本卡片 `AppTextCardMessage` | 可点击跳转的卡片通知 |
| 图文 `AppNewsMessage` | 最多 8 条图文卡片 |

**应用消息支持的发送目标：**
- 指定用户（一个或多个）
- 指定部门
- 指定标签
- 组合目标（用户 + 部门 + 标签）
- 全体成员

## 技术栈

- Java 17
- OkHttp 4.12 (HTTP 客户端)
- Jackson 2.17 (JSON 序列化)
- JUnit 5 + MockWebServer (单元测试)
- Maven (构建工具)

## 快速开始

### 构建项目

```bash
mvn clean compile
```

### 一、群机器人消息

在企业微信群中添加群机器人，获取 Webhook URL 中的 `key` 参数：

```
https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
```

```java
import com.wecombot.client.WeComBotClient;

WeComBotClient client = new WeComBotClient("your-webhook-key");
client.sendText("Hello，这是一条群消息！");
```

### 二、应用消息（定向给用户发私信）

需要以下信息（在企业微信管理后台获取）：
- **corpId** — 企业ID（我的企业 → 企业信息 → 企业ID）
- **corpSecret** — 应用Secret（应用管理 → 自建应用 → Secret）
- **agentId** — 应用AgentId（应用管理 → 自建应用 → AgentId）

```java
import com.wecombot.app.WeComAppClient;
import com.wecombot.app.SendTarget;

WeComAppClient appClient = new WeComAppClient("corpId", "corpSecret", 1000002);

// 给指定用户发消息
appClient.sendText(SendTarget.toUser("zhangsan"), "你好，这是一条私信");
```

## 使用示例

### 群消息

```java
import com.wecombot.client.WeComBotClient;
import com.wecombot.message.TextMessage;

WeComBotClient client = new WeComBotClient("your-key");

// 普通文本
client.sendText("部署完成，服务已上线");

// @所有人
client.send(TextMessage.builder("紧急通知：系统维护").mentionedList("@all").build());

// Markdown
client.sendMarkdown("# 构建报告\n**状态：** <font color=\"info\">成功</font>");
```

### 定向私信

```java
import com.wecombot.app.*;

WeComAppClient appClient = new WeComAppClient("corpId", "corpSecret", 1000002);

// 给单个用户发文本
appClient.sendText(SendTarget.toUser("zhangsan"), "你好！");

// 给多个用户同时发消息
appClient.sendText(SendTarget.toUser("zhangsan", "lisi"), "请查收报表");

// 给用户发 Markdown
appClient.sendMarkdown(SendTarget.toUser("zhangsan"), """
    # 审批通知
    你有一条待审批的报销申请
    **金额：** <font color="warning">¥2,580.00</font>
    """);

// 给用户发文本卡片（可点击跳转）
appClient.send(
    SendTarget.toUser("zhangsan"),
    AppTextCardMessage.builder("审批通知", "李四提交了差旅报销", "https://example.com/approve/123")
        .btnTxt("去审批")
        .build()
);

// 给用户发图文消息
appClient.send(
    SendTarget.toUser("zhangsan"),
    AppNewsMessage.of(
        AppNewsArticle.builder("月度报告", "https://example.com/report")
            .description("点击查看 3 月工作报告")
            .picurl("https://example.com/cover.png")
            .build()
    )
);

// 给整个部门发消息
appClient.sendText(SendTarget.toParty("1"), "部门通知：周五团建");

// 组合目标：同时发给用户和部门
appClient.sendText(
    SendTarget.builder().toUser("zhangsan").toParty("2").build(),
    "系统维护通知"
);

// 发给全体成员
appClient.sendText(SendTarget.toAll(), "全员通知：明天 10 点开会");
```

## 项目结构

```
src/main/java/com/wecombot/
├── client/                            # 群机器人模块
│   ├── WeComBotClient.java            # 群机器人客户端
│   └── WeComBotException.java         # 异常定义
├── message/                           # 群机器人消息类型
│   ├── WeComMessage.java
│   ├── TextMessage.java
│   ├── MarkdownMessage.java
│   ├── ImageMessage.java
│   ├── NewsMessage.java / NewsArticle.java
│   ├── FileMessage.java
│   └── TemplateCardMessage.java
├── app/                               # 应用消息模块（定向私信）
│   ├── WeComAppClient.java            # 应用消息客户端
│   ├── WeComAppException.java         # 异常定义
│   ├── AccessToken.java               # Token 缓存管理
│   ├── SendTarget.java                # 发送目标（用户/部门/标签）
│   ├── AppMessage.java                # 消息接口
│   ├── AppTextMessage.java            # 文本消息
│   ├── AppMarkdownMessage.java        # Markdown 消息
│   ├── AppTextCardMessage.java        # 文本卡片消息
│   ├── AppNewsMessage.java            # 图文消息
│   └── AppNewsArticle.java            # 图文文章
└── demo/                              # 使用示例
    ├── SendTextDemo.java              # 群 - 文本消息
    ├── SendMarkdownDemo.java          # 群 - Markdown
    ├── SendNewsDemo.java              # 群 - 图文
    ├── SendTemplateCardDemo.java      # 群 - 模板卡片
    └── SendDirectMessageDemo.java     # 应用 - 定向私信（全量示例）
```

## 运行测试

```bash
mvn test
```

## 环境变量配置

| 环境变量 | 说明 | 用途 |
|---------|------|------|
| `WECOM_BOT_KEY` | 群机器人 Webhook Key | 群消息 |
| `WECOM_CORP_ID` | 企业 ID | 应用消息 |
| `WECOM_CORP_SECRET` | 应用 Secret | 应用消息 |
| `WECOM_AGENT_ID` | 应用 AgentId | 应用消息 |

## API 参考

- 群机器人：https://developer.work.weixin.qq.com/document/path/91770
- 应用消息：https://developer.work.weixin.qq.com/document/path/90236
- 获取 access_token：https://developer.work.weixin.qq.com/document/path/91039
