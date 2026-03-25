"""示例：发送文本消息到企业微信群。"""

from wecom_bot import WeComBotClient

client = WeComBotClient(key="YOUR_WEBHOOK_KEY")

response = client.send_text("Hello，这是一条测试消息！")
print("发送成功:", response)

response = client.send_text(
    content="注意：系统将于今晚 22:00 进行维护",
    mentioned_list=["@all"],
)
print("@所有人 发送成功:", response)
