package com.wecombot.external;

import java.util.*;

/**
 * 外部群发的目标设置。
 *
 * <p>企业微信外部群发支持两种 chat_type：
 * <ul>
 *   <li>{@code single} — 发给外部客户（以员工名义私聊）</li>
 *   <li>{@code group} — 发到客户群</li>
 * </ul>
 *
 * <p>对于 single 类型，可指定 external_userid 列表（外部客户ID）或
 * 通过 sender 指定发送者（员工userid）。
 */
public class ExternalGroupTarget {

    private final String chatType;
    private final List<String> externalUserIds;
    private final String sender;

    private ExternalGroupTarget(String chatType, List<String> externalUserIds, String sender) {
        this.chatType = chatType;
        this.externalUserIds = externalUserIds;
        this.sender = sender;
    }

    /**
     * 发给指定的外部客户（单聊形式）。
     *
     * @param externalUserIds 外部联系人 userid 列表
     */
    public static ExternalGroupTarget toCustomers(String... externalUserIds) {
        return new ExternalGroupTarget("single", Arrays.asList(externalUserIds), null);
    }

    /**
     * 发给指定的外部客户，指定发送员工。
     *
     * @param sender          发送者（员工 userid）
     * @param externalUserIds 外部联系人 userid 列表
     */
    public static ExternalGroupTarget toCustomers(String sender, List<String> externalUserIds) {
        return new ExternalGroupTarget("single", externalUserIds, sender);
    }

    /**
     * 发到客户群。
     *
     * <p>群发给客户群时，不需要指定具体群 ID，
     * 会发给指定员工（sender）所在的所有客户群。
     *
     * @param sender 发送者（员工 userid）
     */
    public static ExternalGroupTarget toCustomerGroups(String sender) {
        return new ExternalGroupTarget("group", null, sender);
    }

    public void applyTo(Map<String, Object> payload) {
        payload.put("chat_type", chatType);
        if (externalUserIds != null && !externalUserIds.isEmpty()) {
            payload.put("external_userid", externalUserIds);
        }
        if (sender != null && !sender.isEmpty()) {
            payload.put("sender", sender);
        }
    }

    public String getChatType() { return chatType; }
    public List<String> getExternalUserIds() { return externalUserIds; }
    public String getSender() { return sender; }
}
