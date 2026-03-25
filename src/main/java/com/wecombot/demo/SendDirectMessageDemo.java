package com.wecombot.demo;

import com.wecombot.app.*;

import java.util.Map;

/**
 * 示例：定向给指定用户发送私信消息。
 *
 * <p>使用企业微信应用消息 API，需要以下信息：
 * <ul>
 *   <li>corpId — 企业 ID（企业微信管理后台 → 我的企业 → 企业信息 → 企业ID）</li>
 *   <li>corpSecret — 应用 Secret（应用管理 → 自建应用 → Secret）</li>
 *   <li>agentId — 应用 AgentId（应用管理 → 自建应用 → AgentId）</li>
 * </ul>
 *
 * <p>运行前请设置环境变量或替换下方常量值。
 */
public class SendDirectMessageDemo {

    public static void main(String[] args) {
        String corpId = System.getenv().getOrDefault("WECOM_CORP_ID", "YOUR_CORP_ID");
        String corpSecret = System.getenv().getOrDefault("WECOM_CORP_SECRET", "YOUR_CORP_SECRET");
        int agentId = Integer.parseInt(System.getenv().getOrDefault("WECOM_AGENT_ID", "1000002"));

        WeComAppClient appClient = new WeComAppClient(corpId, corpSecret, agentId);

        // ──────────────────────────────────────────────────────────
        // 1. 给指定用户发送文本消息
        // ──────────────────────────────────────────────────────────
        Map<String, Object> result = appClient.sendText(
                SendTarget.toUser("zhangsan"),
                "你好，这是一条定向私信消息 ✅"
        );
        System.out.println("文本消息发送成功: " + result);

        // ──────────────────────────────────────────────────────────
        // 2. 同时给多个用户发消息
        // ──────────────────────────────────────────────────────────
        result = appClient.sendText(
                SendTarget.toUser("zhangsan", "lisi", "wangwu"),
                "各位好，请查收本月绩效报表"
        );
        System.out.println("多人消息发送成功: " + result);

        // ──────────────────────────────────────────────────────────
        // 3. 给指定用户发送 Markdown 消息
        // ──────────────────────────────────────────────────────────
        result = appClient.sendMarkdown(
                SendTarget.toUser("zhangsan"),
                """
                # 审批通知
                
                你有一条待审批的报销申请：
                
                **申请人：** 李四
                **金额：** <font color="warning">¥2,580.00</font>
                **事由：** 客户拜访差旅费
                
                > 请在 24 小时内完成审批
                """
        );
        System.out.println("Markdown 消息发送成功: " + result);

        // ──────────────────────────────────────────────────────────
        // 4. 给指定用户发送文本卡片消息（可点击跳转）
        // ──────────────────────────────────────────────────────────
        result = appClient.send(
                SendTarget.toUser("zhangsan"),
                AppTextCardMessage.builder(
                        "报销审批通知",
                        "<div class=\"gray\">2026年3月25日</div>"
                                + "<div class=\"normal\">李四提交了一笔差旅报销</div>"
                                + "<div class=\"highlight\">金额：¥2,580.00</div>",
                        "https://example.com/approve/12345"
                ).btnTxt("去审批").build()
        );
        System.out.println("文本卡片消息发送成功: " + result);

        // ──────────────────────────────────────────────────────────
        // 5. 给指定用户发送图文消息
        // ──────────────────────────────────────────────────────────
        result = appClient.send(
                SendTarget.toUser("zhangsan"),
                AppNewsMessage.of(
                        AppNewsArticle.builder("月度工作报告已生成", "https://example.com/report/2026-03")
                                .description("点击查看 2026 年 3 月的工作报告详情")
                                .picurl("https://example.com/images/report-cover.png")
                                .build()
                )
        );
        System.out.println("图文消息发送成功: " + result);

        // ──────────────────────────────────────────────────────────
        // 6. 给整个部门发消息
        // ──────────────────────────────────────────────────────────
        result = appClient.sendText(
                SendTarget.toParty("1"),
                "部门通知：本周五下午团建，请准时参加"
        );
        System.out.println("部门消息发送成功: " + result);

        // ──────────────────────────────────────────────────────────
        // 7. 组合目标：同时发给用户和部门
        // ──────────────────────────────────────────────────────────
        result = appClient.sendText(
                SendTarget.builder()
                        .toUser("zhangsan", "lisi")
                        .toParty("2")
                        .build(),
                "重要通知：系统将于今晚 22:00 进行维护升级"
        );
        System.out.println("组合目标消息发送成功: " + result);

        // ──────────────────────────────────────────────────────────
        // 8. 发送给全体成员
        // ──────────────────────────────────────────────────────────
        result = appClient.sendText(
                SendTarget.toAll(),
                "全员通知：明天上午 10 点召开全体大会"
        );
        System.out.println("全体消息发送成功: " + result);
    }
}
