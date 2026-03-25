package com.wecombot.demo;

import com.wecombot.client.WeComBotClient;
import com.wecombot.message.TemplateCardMessage;

import java.util.List;
import java.util.Map;

/**
 * 示例：发送模板卡片消息到企业微信群。
 */
public class SendTemplateCardDemo {

    public static void main(String[] args) {
        String key = System.getenv().getOrDefault("WECOM_BOT_KEY", "YOUR_WEBHOOK_KEY");
        WeComBotClient client = new WeComBotClient(key);

        TemplateCardMessage card = TemplateCardMessage
                .builder("订单异常告警", "https://example.com/alerts/12345")
                .sourceDesc("监控中心")
                .subTitleText("订单处理延迟超过阈值，请及时关注。")
                .emphasisContentTitle("128")
                .emphasisContentDesc("待处理订单数")
                .horizontalContentList(List.of(
                        Map.of("keyname", "告警级别", "value", "P1 - 紧急"),
                        Map.of("keyname", "影响范围", "value", "华东区域"),
                        Map.of("keyname", "持续时间", "value", "15 分钟")
                ))
                .jumpList(List.of(
                        Map.of("type", "1", "title", "查看详情", "url", "https://example.com/alerts/12345")
                ))
                .build();

        Map<String, Object> result = client.send(card);
        System.out.println("模板卡片消息发送成功: " + result);
    }
}
