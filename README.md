# 企业微信消息通知 SDK

基于 Java 17 + OkHttp + Jackson 实现的企业微信消息通知 SDK，覆盖三种消息推送场景：

## 三种方案对比

| 方案 | 客户端 | 适用场景 | 接收方 | 限制 |
|------|--------|---------|--------|------|
| **群机器人** | `WeComBotClient` | 内部群通知、CI/CD、监控告警 | 内部群成员 | 仅限内部群，不支持外部群 |
| **应用消息** | `WeComAppClient` | 定向私信、审批通知、任务提醒 | 企业内部员工 | 需要自建应用，员工需在可见范围 |
| **客户联系群发** | `WeComExternalClient` | 客户营销、外部群通知、客户关怀 | **外部客户 / 客户群** | 以员工名义发出，每客户每天 1 条 |

> **注意：** 企业微信群机器人（Webhook）**只能在内部群使用**，无法往含外部联系人的群发消息。
> 要给外部客户/外部群发消息，请使用 `WeComExternalClient`（客户联系群发 API）。

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

---

### 方案一：群机器人（内部群消息）

在企业微信内部群中添加群机器人，获取 Webhook Key。

```java
import com.wecombot.client.WeComBotClient;

WeComBotClient client = new WeComBotClient("your-webhook-key");

client.sendText("Hello，这是一条群消息！");
client.sendMarkdown("# 部署通知\n**状态：** <font color=\"info\">成功</font>");
```

支持的消息类型：文本、Markdown、图片、图文、文件、模板卡片。

---

### 方案二：应用消息（定向给内部员工私信）

需要在管理后台创建自建应用，获取 corpId、corpSecret、agentId。

```java
import com.wecombot.app.WeComAppClient;
import com.wecombot.app.SendTarget;

WeComAppClient appClient = new WeComAppClient("corpId", "corpSecret", 1000002);

// 给指定用户发消息
appClient.sendText(SendTarget.toUser("zhangsan"), "你好，这是一条私信");

// 给多个用户发
appClient.sendText(SendTarget.toUser("zhangsan", "lisi"), "请查收报表");

// 给部门发
appClient.sendText(SendTarget.toParty("1"), "部门通知");

// 给全体发
appClient.sendText(SendTarget.toAll(), "全员通知");
```

支持的消息类型：文本、Markdown、文本卡片、图文。

---

### 方案三：客户联系群发（给外部客户/客户群发消息）

需要在管理后台 → 客户联系 → API 中获取「客户联系」Secret（注意和自建应用 Secret 不同）。

```java
import com.wecombot.external.*;

WeComExternalClient externalClient = new WeComExternalClient("corpId", "externalSecret");

// 给外部客户发私聊消息（以员工名义）
externalClient.sendText(
    ExternalGroupTarget.toCustomers("employee_id", List.of("external_user_1")),
    "你好！我们的新品已上线，欢迎了解。"
);

// 往客户群发消息
externalClient.sendText(
    ExternalGroupTarget.toCustomerGroups("employee_id"),
    "各位群友好，本周优惠活动开始了！"
);

// 发链接给外部客户
externalClient.send(
    ExternalGroupTarget.toCustomers("employee_id", List.of("external_user_1")),
    "为您推荐：",
    ExternalLinkMessage.builder("新品推荐", "https://example.com/products")
        .desc("限时 8 折优惠")
        .picurl("https://example.com/cover.jpg")
        .build()
);

// 发小程序卡片
externalClient.send(
    ExternalGroupTarget.toCustomers("employee_id", List.of("external_user_1")),
    "点击查看专属优惠：",
    ExternalMiniprogramMessage.builder("优惠券", "wx123", "/pages/coupon", "pic_media_id")
        .build()
);

// 查询群发结果
Map<String, Object> result = externalClient.getGroupMsgResult("msgid", "", 100);
```

**客户联系群发注意事项：**
- 消息以**员工名义**发出，不是以机器人名义
- 每个客户每天最多收到 **1 条**群发消息
- 群发需要员工在手机端确认后才会真正发出
- 发送者（员工）需要在「客户联系」功能的使用范围内
- 目标客户需要已经被添加为员工的外部联系人

## 项目结构

```
src/main/java/com/wecombot/
├── client/                            # 方案一：群机器人
│   ├── WeComBotClient.java
│   └── WeComBotException.java
├── message/                           # 群机器人消息类型
│   ├── WeComMessage.java
│   ├── TextMessage.java
│   ├── MarkdownMessage.java
│   ├── ImageMessage.java
│   ├── NewsMessage.java / NewsArticle.java
│   ├── FileMessage.java
│   └── TemplateCardMessage.java
├── app/                               # 方案二：应用消息（定向私信）
│   ├── WeComAppClient.java
│   ├── WeComAppException.java
│   ├── AccessToken.java
│   ├── SendTarget.java
│   ├── AppMessage.java
│   ├── AppTextMessage.java
│   ├── AppMarkdownMessage.java
│   ├── AppTextCardMessage.java
│   ├── AppNewsMessage.java
│   └── AppNewsArticle.java
├── external/                          # 方案三：客户联系群发（外部客户/客户群）
│   ├── WeComExternalClient.java
│   ├── WeComExternalException.java
│   ├── ExternalGroupTarget.java
│   ├── ExternalMessage.java
│   ├── ExternalImageMessage.java
│   ├── ExternalLinkMessage.java
│   └── ExternalMiniprogramMessage.java
└── demo/                              # 使用示例
    ├── SendTextDemo.java
    ├── SendMarkdownDemo.java
    ├── SendNewsDemo.java
    ├── SendTemplateCardDemo.java
    ├── SendDirectMessageDemo.java
    └── SendExternalMessageDemo.java   # 外部客户群发示例
```

## 运行测试

```bash
mvn test
```

## 环境变量配置

| 环境变量 | 说明 | 用途 |
|---------|------|------|
| `WECOM_BOT_KEY` | 群机器人 Webhook Key | 群消息 |
| `WECOM_CORP_ID` | 企业 ID | 应用消息 / 客户联系 |
| `WECOM_CORP_SECRET` | 自建应用 Secret | 应用消息 |
| `WECOM_AGENT_ID` | 自建应用 AgentId | 应用消息 |
| `WECOM_EXTERNAL_SECRET` | 客户联系 Secret | 客户联系群发 |

## API 参考

- 群机器人：https://developer.work.weixin.qq.com/document/path/91770
- 应用消息：https://developer.work.weixin.qq.com/document/path/90236
- 客户联系群发：https://developer.work.weixin.qq.com/document/path/92135
- 获取 access_token：https://developer.work.weixin.qq.com/document/path/91039
