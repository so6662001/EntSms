package com.wecombot.app;

import java.util.*;

/**
 * 应用文本卡片消息。
 *
 * <p>适合需要跳转链接的通知场景，卡片整体可点击跳转。
 */
public class AppTextCardMessage implements AppMessage {

    private final String title;
    private final String description;
    private final String url;
    private final String btnTxt;

    private AppTextCardMessage(Builder builder) {
        this.title = Objects.requireNonNull(builder.title, "title 不能为空");
        this.description = Objects.requireNonNull(builder.description, "description 不能为空");
        this.url = Objects.requireNonNull(builder.url, "url 不能为空");
        this.btnTxt = builder.btnTxt;
    }

    public static Builder builder(String title, String description, String url) {
        return new Builder(title, description, url);
    }

    @Override
    public String getMsgType() {
        return "textcard";
    }

    @Override
    public Map<String, Object> getBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("title", title);
        body.put("description", description);
        body.put("url", url);
        if (btnTxt != null && !btnTxt.isEmpty()) {
            body.put("btntxt", btnTxt);
        }
        return body;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getUrl() { return url; }
    public String getBtnTxt() { return btnTxt; }

    public static class Builder {
        private final String title;
        private final String description;
        private final String url;
        private String btnTxt;

        private Builder(String title, String description, String url) {
            this.title = title;
            this.description = description;
            this.url = url;
        }

        /** 按钮文字，默认为 "详情"。 */
        public Builder btnTxt(String btnTxt) {
            this.btnTxt = btnTxt;
            return this;
        }

        public AppTextCardMessage build() {
            return new AppTextCardMessage(this);
        }
    }
}
