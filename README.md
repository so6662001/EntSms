# 企业微信群机器人消息通知 SDK

基于 Java 17 + OkHttp + Jackson 实现的企业微信群机器人 Webhook 消息通知 SDK。

## 功能特性

- **文本消息** — 支持 @指定成员 / @所有人
- **Markdown 消息** — 支持标题、引用、加粗、颜色、表格等
- **图片消息** — 自动 Base64 编码 + MD5 计算
- **图文消息** — 最多 8 条图文卡片
- **文件消息** — 自动上传 + 发送
- **模板卡片消息** — 文本通知型模板卡片
- Builder 模式构造消息，API 简洁易用
- 完善的错误处理和异常封装

## 技术栈

- Java 17
- OkHttp 4.12 (HTTP 客户端)
- Jackson 2.17 (JSON 序列化)
- JUnit 5 + MockWebServer (单元测试)
- Maven (构建工具)

## 快速开始

### 1. 构建项目

```bash
mvn clean compile
```

### 2. 获取 Webhook Key

在企业微信群中添加群机器人，获取 Webhook URL，从中提取 `key` 参数：

```
https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
                                                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
                                                      这就是你的 key
```

### 3. 发送消息

```java
import com.wecombot.client.WeComBotClient;

WeComBotClient client = new WeComBotClient("your-webhook-key");
client.sendText("Hello，这是一条测试消息！");
```

也可以通过环境变量读取 key：

```java
String key = System.getenv("WECOM_BOT_KEY");
WeComBotClient client = new WeComBotClient(key);
```

## 使用示例

### 发送文本消息

```java
import com.wecombot.client.WeComBotClient;
import com.wecombot.message.TextMessage;

WeComBotClient client = new WeComBotClient("your-key");

// 普通文本
client.sendText("部署完成，服务已上线");

// @指定成员
client.send(
    TextMessage.builder("请审核本次发布")
        .mentionedList("zhangsan", "lisi")
        .build()
);

// @所有人
client.send(
    TextMessage.builder("紧急通知：系统维护")
        .mentionedList("@all")
        .build()
);

// @手机号
client.send(
    TextMessage.builder("请及时处理")
        .mentionedMobileList("13800138000")
        .build()
);
```

### 发送 Markdown 消息

```java
client.sendMarkdown("""
    # 构建报告

    **项目：** MyApp
    **分支：** main
    **状态：** <font color="info">成功</font>

    > 构建耗时 2m 15s
    """);
```

### 发送图文消息

```java
import com.wecombot.message.NewsArticle;
import com.wecombot.message.NewsMessage;

NewsMessage news = NewsMessage.of(
    NewsArticle.builder("新版本发布", "https://example.com/release")
        .description("v2.0 包含重大更新")
        .picurl("https://example.com/cover.png")
        .build()
);
client.send(news);
```

### 发送图片

```java
import java.nio.file.Path;

client.sendImage(Path.of("screenshot.png"));

// 或者从 byte[] 发送
client.sendImage(imageBytes);
```

### 发送文件

```java
client.sendFile(Path.of("report.pdf"));
```

### 发送模板卡片

```java
import com.wecombot.message.TemplateCardMessage;

TemplateCardMessage card = TemplateCardMessage
    .builder("告警通知", "https://example.com/alert/1")
    .sourceDesc("监控中心")
    .emphasisContentTitle("3")
    .emphasisContentDesc("待处理告警")
    .horizontalContentList(List.of(
        Map.of("keyname", "级别", "value", "P1"),
        Map.of("keyname", "来源", "value", "监控系统")
    ))
    .build();
client.send(card);
```

## 项目结构

```
.
├── pom.xml
├── src/
│   ├── main/java/com/wecombot/
│   │   ├── client/
│   │   │   ├── WeComBotClient.java        # 核心客户端
│   │   │   └── WeComBotException.java     # 异常定义
│   │   ├── message/
│   │   │   ├── WeComMessage.java          # 消息接口
│   │   │   ├── TextMessage.java           # 文本消息
│   │   │   ├── MarkdownMessage.java       # Markdown 消息
│   │   │   ├── ImageMessage.java          # 图片消息
│   │   │   ├── NewsMessage.java           # 图文消息
│   │   │   ├── NewsArticle.java           # 图文文章
│   │   │   ├── FileMessage.java           # 文件消息
│   │   │   └── TemplateCardMessage.java   # 模板卡片消息
│   │   └── demo/
│   │       ├── SendTextDemo.java          # 文本消息示例
│   │       ├── SendMarkdownDemo.java      # Markdown 示例
│   │       ├── SendNewsDemo.java          # 图文消息示例
│   │       └── SendTemplateCardDemo.java  # 模板卡片示例
│   └── test/java/com/wecombot/
│       ├── MessageTest.java               # 消息序列化测试
│       └── ClientTest.java                # 客户端测试
├── .gitignore
└── README.md
```

## 运行测试

```bash
mvn test
```

## API 参考

企业微信群机器人官方文档：https://developer.work.weixin.qq.com/document/path/91770
