package com.wecombot.demo;

import com.wecombot.client.WeComBotClient;

import java.util.Map;

/**
 * 示例：发送 Markdown 消息到企业微信群。
 */
public class SendMarkdownDemo {

    public static void main(String[] args) {
        String key = System.getenv().getOrDefault("WECOM_BOT_KEY", "YOUR_WEBHOOK_KEY");
        WeComBotClient client = new WeComBotClient(key);

        String markdown = """
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
                """;

        Map<String, Object> result = client.sendMarkdown(markdown);
        System.out.println("Markdown 消息发送成功: " + result);
    }
}
