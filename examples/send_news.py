"""示例：发送图文消息到企业微信群。"""

from wecom_bot import WeComBotClient, NewsMessage, NewsArticle

client = WeComBotClient(key="YOUR_WEBHOOK_KEY")

news = NewsMessage(
    articles=[
        NewsArticle(
            title="v2.4.0 版本发布",
            description="本次更新包含多项性能优化和 bug 修复，建议尽快升级。",
            url="https://example.com/releases/v2.4.0",
            picurl="https://example.com/images/release.png",
        ),
        NewsArticle(
            title="API 文档更新",
            description="新增了批量操作相关接口文档。",
            url="https://example.com/docs/api",
        ),
    ]
)

response = client.send(news)
print("图文消息发送成功:", response)
