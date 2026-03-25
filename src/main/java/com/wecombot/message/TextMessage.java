package com.wecombot.message;

import java.util.*;

/**
 * 文本消息。
 *
 * <p>支持 @指定成员 和 @所有人。
 * 文本内容最长 2048 字节。
 */
public class TextMessage implements WeComMessage {

    private final String content;
    private final List<String> mentionedList;
    private final List<String> mentionedMobileList;

    private TextMessage(Builder builder) {
        this.content = Objects.requireNonNull(builder.content, "content 不能为空");
        this.mentionedList = builder.mentionedList;
        this.mentionedMobileList = builder.mentionedMobileList;
    }

    public static Builder builder(String content) {
        return new Builder(content);
    }

    /** 快捷构造：仅文本内容。 */
    public static TextMessage of(String content) {
        return builder(content).build();
    }

    @Override
    public Map<String, Object> toPayload() {
        Map<String, Object> text = new LinkedHashMap<>();
        text.put("content", content);
        if (mentionedList != null && !mentionedList.isEmpty()) {
            text.put("mentioned_list", mentionedList);
        }
        if (mentionedMobileList != null && !mentionedMobileList.isEmpty()) {
            text.put("mentioned_mobile_list", mentionedMobileList);
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("msgtype", "text");
        payload.put("text", text);
        return payload;
    }

    public String getContent() { return content; }
    public List<String> getMentionedList() { return mentionedList; }
    public List<String> getMentionedMobileList() { return mentionedMobileList; }

    public static class Builder {
        private final String content;
        private List<String> mentionedList;
        private List<String> mentionedMobileList;

        private Builder(String content) {
            this.content = content;
        }

        public Builder mentionedList(List<String> mentionedList) {
            this.mentionedList = mentionedList;
            return this;
        }

        public Builder mentionedList(String... userIds) {
            this.mentionedList = Arrays.asList(userIds);
            return this;
        }

        public Builder mentionedMobileList(List<String> mentionedMobileList) {
            this.mentionedMobileList = mentionedMobileList;
            return this;
        }

        public Builder mentionedMobileList(String... mobiles) {
            this.mentionedMobileList = Arrays.asList(mobiles);
            return this;
        }

        public TextMessage build() {
            return new TextMessage(this);
        }
    }
}
