package com.wecombot.demo;

import com.wecombot.client.WeComBotClient;
import com.wecombot.message.NewsArticle;
import com.wecombot.message.NewsMessage;

import java.util.Map;

/**
 * 示例：发送图文消息到企业微信群。
 */
public class SendNewsDemo {

    public static void main(String[] args) {
        String key = System.getenv().getOrDefault("WECOM_BOT_KEY", "YOUR_WEBHOOK_KEY");
        WeComBotClient client = new WeComBotClient(key);

        NewsMessage news = NewsMessage.of(
                NewsArticle.builder("v2.4.0 版本发布", "https://example.com/releases/v2.4.0")
                        .description("本次更新包含多项性能优化和 bug 修复，建议尽快升级。")
                        .picurl("https://example.com/images/release.png")
                        .build(),
                NewsArticle.builder("API 文档更新", "https://example.com/docs/api")
                        .description("新增了批量操作相关接口文档。")
                        .build()
        );

        Map<String, Object> result = client.send(news);
        System.out.println("图文消息发送成功: " + result);
    }
}
