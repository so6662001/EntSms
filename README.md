# 企业微信群机器人消息通知 Demo

通过企业微信群机器人 Webhook 接口，向外部群发送消息通知。

## 功能特性

- 支持**文本消息**（可 @指定成员 / @所有人）
- 支持 **Markdown 消息**（标题、引用、加粗、颜色、表格等）
- 支持**图片消息**（自动 base64 编码）
- 支持**图文消息**（最多 8 条图文卡片）
- 支持**文件消息**（自动上传 + 发送）
- 支持**模板卡片消息**（文本通知型）
- 通过 `.env` 文件管理 Webhook Key 配置

## 快速开始

### 1. 安装依赖

```bash
pip install -r requirements.txt
```

### 2. 获取 Webhook Key

在企业微信群中添加群机器人，获取 Webhook URL，从中提取 `key` 参数：

```
https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
                                                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
                                                      这就是你的 key
```

### 3. 配置环境变量

复制 `.env.example` 为 `.env` 并填入你的 key：

```bash
cp .env.example .env
```

编辑 `.env` 文件：

```
WECOM_BOT_KEY=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
```

### 4. 发送消息

**方式一：直接指定 Key**

```python
from wecom_bot import WeComBotClient

client = WeComBotClient(key="your-webhook-key")
client.send_text("Hello，这是一条测试消息！")
```

**方式二：从环境变量读取**

```python
from wecom_bot.config import create_client_from_env

client = create_client_from_env()
client.send_text("通过环境变量发送的消息 ✅")
```

## 使用示例

### 发送文本消息

```python
from wecom_bot import WeComBotClient

client = WeComBotClient(key="your-key")

# 普通文本
client.send_text("部署完成，服务已上线")

# @指定成员
client.send_text(
    content="请审核本次发布",
    mentioned_list=["zhangsan", "lisi"],
)

# @所有人
client.send_text(
    content="紧急通知：系统维护",
    mentioned_list=["@all"],
)
```

### 发送 Markdown 消息

```python
client.send_markdown("""\
# 构建报告

**项目：** MyApp
**分支：** main
**状态：** <font color="info">成功</font>

> 构建耗时 2m 15s
""")
```

### 发送图文消息

```python
from wecom_bot import NewsMessage, NewsArticle

news = NewsMessage(articles=[
    NewsArticle(
        title="新版本发布",
        description="v2.0 包含重大更新",
        url="https://example.com/release",
        picurl="https://example.com/cover.png",
    ),
])
client.send(news)
```

### 发送图片

```python
client.send_image("screenshot.png")
```

### 发送文件

```python
client.send_file("report.pdf")
```

### 发送模板卡片

```python
from wecom_bot import TemplateCardMessage

card = TemplateCardMessage(
    main_title="告警通知",
    card_action_url="https://example.com/alert/1",
    emphasis_content_title="3",
    emphasis_content_desc="待处理告警",
    horizontal_content_list=[
        {"keyname": "级别", "value": "P1"},
        {"keyname": "来源", "value": "监控系统"},
    ],
)
client.send(card)
```

## 项目结构

```
.
├── wecom_bot/
│   ├── __init__.py       # 包入口，导出公开 API
│   ├── client.py         # 核心客户端（发送 / 上传）
│   ├── config.py         # 配置管理（环境变量 / .env）
│   └── message.py        # 消息类型定义
├── examples/
│   ├── send_text.py          # 文本消息示例
│   ├── send_markdown.py      # Markdown 消息示例
│   ├── send_news.py          # 图文消息示例
│   ├── send_template_card.py # 模板卡片示例
│   └── send_with_env.py      # 环境变量配置示例
├── tests/
│   └── test_wecom_bot.py # 单元测试
├── .env.example          # 环境变量模板
├── .gitignore
├── requirements.txt
└── README.md
```

## 运行测试

```bash
pytest tests/ -v
```

## API 参考

企业微信群机器人官方文档：https://developer.work.weixin.qq.com/document/path/91770
