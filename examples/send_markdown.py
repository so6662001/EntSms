"""示例：发送 Markdown 消息到企业微信群。"""

from wecom_bot import WeComBotClient

client = WeComBotClient(key="YOUR_WEBHOOK_KEY")

markdown_content = """\
# 部署通知

> 应用：**用户服务**

**环境：** 生产环境
**版本：** v2.3.1
**状态：** <font color="info">部署成功</font>

---

| 指标 | 数值 |
|------|------|
| 部署耗时 | 3m 20s |
| 健康检查 | 通过 |
| 回滚版本 | v2.3.0 |
"""

response = client.send_markdown(markdown_content)
print("Markdown 消息发送成功:", response)
