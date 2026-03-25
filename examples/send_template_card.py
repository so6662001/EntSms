"""示例：发送模板卡片消息到企业微信群。"""

from wecom_bot import WeComBotClient, TemplateCardMessage

client = WeComBotClient(key="YOUR_WEBHOOK_KEY")

card = TemplateCardMessage(
    main_title="订单异常告警",
    card_action_url="https://example.com/alerts/12345",
    source_desc="监控中心",
    sub_title_text="订单处理延迟超过阈值，请及时关注。",
    emphasis_content_title="128",
    emphasis_content_desc="待处理订单数",
    horizontal_content_list=[
        {"keyname": "告警级别", "value": "P1 - 紧急"},
        {"keyname": "影响范围", "value": "华东区域"},
        {"keyname": "持续时间", "value": "15 分钟"},
    ],
    jump_list=[
        {"type": 1, "title": "查看详情", "url": "https://example.com/alerts/12345"},
    ],
)

response = client.send(card)
print("模板卡片消息发送成功:", response)
