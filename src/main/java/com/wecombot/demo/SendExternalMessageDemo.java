package com.wecombot.demo;

import com.wecombot.external.*;

import java.util.List;
import java.util.Map;

/**
 * 示例：向外部客户 / 客户群发送消息。
 *
 * <p>企业微信群机器人（Webhook）只能在内部群使用，无法向外部群发消息。
 * 要给外部客户发消息，需要使用「客户联系 - 群发消息」API。
 *
 * <h3>前置条件</h3>
 * <ol>
 *   <li>企业微信管理后台 → 客户联系 → API，获取「客户联系」的 Secret</li>
 *   <li>注意：这个 Secret 和自建应用的 Secret 不同</li>
 *   <li>发送者（员工）需要在「客户联系」功能的使用范围内</li>
 *   <li>目标客户需要已经被添加为员工的外部联系人</li>
 * </ol>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>群发消息以<b>员工名义</b>发出，不是以机器人名义</li>
 *   <li>每个客户每天最多收到 1 条群发消息</li>
 *   <li>群发需要员工在手机端确认后才会真正发出</li>
 * </ul>
 */
public class SendExternalMessageDemo {

    public static void main(String[] args) {
        String corpId = System.getenv().getOrDefault("WECOM_CORP_ID", "YOUR_CORP_ID");
        String externalSecret = System.getenv().getOrDefault("WECOM_EXTERNAL_SECRET", "YOUR_EXTERNAL_SECRET");

        WeComExternalClient client = new WeComExternalClient(corpId, externalSecret);

        // ──────────────────────────────────────────────────────────
        // 1. 给外部客户发纯文本消息（以员工名义私聊）
        // ──────────────────────────────────────────────────────────
        Map<String, Object> result = client.sendText(
                ExternalGroupTarget.toCustomers("employee_userid",
                        List.of("external_user_id_1", "external_user_id_2")),
                "你好！我们的新品已上线，欢迎了解。"
        );
        System.out.println("外部客户文本消息: " + result);
        String msgid = (String) result.get("msgid");

        // ──────────────────────────────────────────────────────────
        // 2. 给外部客户发文本 + 链接附件
        // ──────────────────────────────────────────────────────────
        result = client.send(
                ExternalGroupTarget.toCustomers("employee_userid",
                        List.of("external_user_id_1")),
                "为您推荐本周精选产品：",
                ExternalLinkMessage.builder("新品推荐 - 春季限定", "https://example.com/products/spring")
                        .desc("2026 春季限定款，限时 8 折优惠")
                        .picurl("https://example.com/images/spring-product.jpg")
                        .build()
        );
        System.out.println("外部客户链接消息: " + result);

        // ──────────────────────────────────────────────────────────
        // 3. 给外部客户发图片
        // ──────────────────────────────────────────────────────────
        result = client.send(
                ExternalGroupTarget.toCustomers("employee_userid",
                        List.of("external_user_id_1")),
                ExternalImageMessage.ofMediaId("MEDIA_ID_FROM_UPLOAD")
        );
        System.out.println("外部客户图片消息: " + result);

        // ──────────────────────────────────────────────────────────
        // 4. 往客户群发文本消息
        // ──────────────────────────────────────────────────────────
        result = client.sendText(
                ExternalGroupTarget.toCustomerGroups("employee_userid"),
                "各位群友好，本周优惠活动开始了！"
        );
        System.out.println("客户群文本消息: " + result);

        // ──────────────────────────────────────────────────────────
        // 5. 往客户群发文本 + 链接
        // ──────────────────────────────────────────────────────────
        result = client.send(
                ExternalGroupTarget.toCustomerGroups("employee_userid"),
                "最新活动通知：",
                ExternalLinkMessage.builder("周末限时折扣", "https://example.com/sale")
                        .desc("全场满 200 减 50，仅限本周末")
                        .build()
        );
        System.out.println("客户群链接消息: " + result);

        // ──────────────────────────────────────────────────────────
        // 6. 发送小程序卡片给外部客户
        // ──────────────────────────────────────────────────────────
        result = client.send(
                ExternalGroupTarget.toCustomers("employee_userid",
                        List.of("external_user_id_1")),
                "点击小程序查看您的专属优惠：",
                ExternalMiniprogramMessage.builder(
                        "我的专属优惠券",
                        "wx1234567890",
                        "/pages/coupon/index?userId=ext_001",
                        "MINIPROGRAM_PIC_MEDIA_ID"
                ).build()
        );
        System.out.println("小程序消息: " + result);

        // ──────────────────────────────────────────────────────────
        // 7. 查询群发结果
        // ──────────────────────────────────────────────────────────
        if (msgid != null) {
            Map<String, Object> sendResult = client.getGroupMsgResult(msgid, "", 100);
            System.out.println("群发结果: " + sendResult);
        }
    }
}
