package com.wecombot.message;

import java.util.*;

/**
 * 文本通知型模板卡片消息。
 */
public class TemplateCardMessage implements WeComMessage {

    private final String mainTitle;
    private final String cardActionUrl;
    private final String cardType;
    private final String sourceIconUrl;
    private final String sourceDesc;
    private final String subTitleText;
    private final String emphasisContentTitle;
    private final String emphasisContentDesc;
    private final List<Map<String, String>> horizontalContentList;
    private final List<Map<String, String>> jumpList;

    private TemplateCardMessage(Builder builder) {
        this.mainTitle = Objects.requireNonNull(builder.mainTitle, "mainTitle 不能为空");
        this.cardActionUrl = Objects.requireNonNull(builder.cardActionUrl, "cardActionUrl 不能为空");
        this.cardType = builder.cardType;
        this.sourceIconUrl = builder.sourceIconUrl;
        this.sourceDesc = builder.sourceDesc;
        this.subTitleText = builder.subTitleText;
        this.emphasisContentTitle = builder.emphasisContentTitle;
        this.emphasisContentDesc = builder.emphasisContentDesc;
        this.horizontalContentList = builder.horizontalContentList;
        this.jumpList = builder.jumpList;
    }

    public static Builder builder(String mainTitle, String cardActionUrl) {
        return new Builder(mainTitle, cardActionUrl);
    }

    @Override
    public Map<String, Object> toPayload() {
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("card_type", cardType);
        card.put("main_title", Map.of("title", mainTitle));
        card.put("card_action", Map.of("type", 1, "url", cardActionUrl));

        if (isNotEmpty(sourceIconUrl) || isNotEmpty(sourceDesc)) {
            Map<String, Object> source = new LinkedHashMap<>();
            if (isNotEmpty(sourceIconUrl)) source.put("icon_url", sourceIconUrl);
            if (isNotEmpty(sourceDesc)) source.put("desc", sourceDesc);
            card.put("source", source);
        }

        if (isNotEmpty(subTitleText)) {
            card.put("sub_title_text", subTitleText);
        }

        if (isNotEmpty(emphasisContentTitle)) {
            Map<String, String> emphasis = new LinkedHashMap<>();
            emphasis.put("title", emphasisContentTitle);
            if (isNotEmpty(emphasisContentDesc)) {
                emphasis.put("desc", emphasisContentDesc);
            }
            card.put("emphasis_content", emphasis);
        }

        if (horizontalContentList != null && !horizontalContentList.isEmpty()) {
            card.put("horizontal_content_list", horizontalContentList);
        }

        if (jumpList != null && !jumpList.isEmpty()) {
            card.put("jump_list", jumpList);
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("msgtype", "template_card");
        payload.put("template_card", card);
        return payload;
    }

    private static boolean isNotEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    public static class Builder {
        private final String mainTitle;
        private final String cardActionUrl;
        private String cardType = "text_notice";
        private String sourceIconUrl;
        private String sourceDesc;
        private String subTitleText;
        private String emphasisContentTitle;
        private String emphasisContentDesc;
        private List<Map<String, String>> horizontalContentList;
        private List<Map<String, String>> jumpList;

        private Builder(String mainTitle, String cardActionUrl) {
            this.mainTitle = mainTitle;
            this.cardActionUrl = cardActionUrl;
        }

        public Builder cardType(String cardType) { this.cardType = cardType; return this; }
        public Builder sourceIconUrl(String sourceIconUrl) { this.sourceIconUrl = sourceIconUrl; return this; }
        public Builder sourceDesc(String sourceDesc) { this.sourceDesc = sourceDesc; return this; }
        public Builder subTitleText(String subTitleText) { this.subTitleText = subTitleText; return this; }
        public Builder emphasisContentTitle(String title) { this.emphasisContentTitle = title; return this; }
        public Builder emphasisContentDesc(String desc) { this.emphasisContentDesc = desc; return this; }

        public Builder horizontalContentList(List<Map<String, String>> list) {
            this.horizontalContentList = list;
            return this;
        }

        public Builder jumpList(List<Map<String, String>> list) {
            this.jumpList = list;
            return this;
        }

        public TemplateCardMessage build() {
            return new TemplateCardMessage(this);
        }
    }
}
