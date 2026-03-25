package com.wecombot.demo;

import com.wecombot.client.WeComBotClient;
import com.wecombot.message.TextMessage;

import java.util.List;
import java.util.Map;

/**
 * 示例：发送文本消息到企业微信群。
 *
 * <p>运行前请替换 YOUR_WEBHOOK_KEY 为实际的 Webhook Key。
 */
public class SendTextDemo {

    public static void main(String[] args) {
        String key = System.getenv().getOrDefault("WECOM_BOT_KEY", "YOUR_WEBHOOK_KEY");
        WeComBotClient client = new WeComBotClient(key);

        // 1. 发送简单文本
        Map<String, Object> result = client.sendText("Hello，这是一条测试消息！");
        System.out.println("发送成功: " + result);

        // 2. 发送文本并 @所有人
        result = client.send(
                TextMessage.builder("注意：系统将于今晚 22:00 进行维护")
                        .mentionedList("@all")
                        .build()
        );
        System.out.println("@所有人 发送成功: " + result);

        // 3. 发送文本并 @指定成员
        result = client.send(
                TextMessage.builder("请审核本次发布")
                        .mentionedList("zhangsan", "lisi")
                        .mentionedMobileList("13800138000")
                        .build()
        );
        System.out.println("@指定成员 发送成功: " + result);
    }
}
