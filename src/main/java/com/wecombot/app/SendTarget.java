package com.wecombot.app;

import java.util.*;

/**
 * 消息发送目标。
 *
 * <p>可以指定用户、部门、标签，三者取并集。
 * 使用 toUser("@all") 表示发送给全体成员。
 */
public class SendTarget {

    private final String toUser;
    private final String toParty;
    private final String toTag;

    private SendTarget(String toUser, String toParty, String toTag) {
        this.toUser = toUser;
        this.toParty = toParty;
        this.toTag = toTag;
    }

    /** 发送给指定用户（多个用 | 分隔）。 */
    public static SendTarget toUser(String... userIds) {
        return new SendTarget(String.join("|", userIds), null, null);
    }

    /** 发送给指定部门（多个用 | 分隔）。 */
    public static SendTarget toParty(String... partyIds) {
        return new SendTarget(null, String.join("|", partyIds), null);
    }

    /** 发送给指定标签（多个用 | 分隔）。 */
    public static SendTarget toTag(String... tagIds) {
        return new SendTarget(null, null, String.join("|", tagIds));
    }

    /** 发送给全体成员。 */
    public static SendTarget toAll() {
        return new SendTarget("@all", null, null);
    }

    /** 自定义组合目标。 */
    public static Builder builder() {
        return new Builder();
    }

    public void applyTo(Map<String, Object> payload) {
        if (toUser != null) payload.put("touser", toUser);
        if (toParty != null) payload.put("toparty", toParty);
        if (toTag != null) payload.put("totag", toTag);
    }

    public String getToUser() { return toUser; }
    public String getToParty() { return toParty; }
    public String getToTag() { return toTag; }

    public static class Builder {
        private String toUser;
        private String toParty;
        private String toTag;

        public Builder toUser(String... userIds) {
            this.toUser = String.join("|", userIds);
            return this;
        }

        public Builder toParty(String... partyIds) {
            this.toParty = String.join("|", partyIds);
            return this;
        }

        public Builder toTag(String... tagIds) {
            this.toTag = String.join("|", tagIds);
            return this;
        }

        public SendTarget build() {
            if (toUser == null && toParty == null && toTag == null) {
                throw new IllegalArgumentException("至少需要指定一个发送目标（toUser/toParty/toTag）");
            }
            return new SendTarget(toUser, toParty, toTag);
        }
    }
}
