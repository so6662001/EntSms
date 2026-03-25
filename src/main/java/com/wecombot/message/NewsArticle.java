package com.wecombot.message;

import java.util.*;

/**
 * 图文消息中的单篇文章。
 */
public class NewsArticle {

    private final String title;
    private final String url;
    private final String description;
    private final String picurl;

    private NewsArticle(Builder builder) {
        this.title = Objects.requireNonNull(builder.title, "title 不能为空");
        this.url = Objects.requireNonNull(builder.url, "url 不能为空");
        this.description = builder.description;
        this.picurl = builder.picurl;
    }

    public static Builder builder(String title, String url) {
        return new Builder(title, url);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("title", title);
        map.put("url", url);
        if (description != null && !description.isEmpty()) {
            map.put("description", description);
        }
        if (picurl != null && !picurl.isEmpty()) {
            map.put("picurl", picurl);
        }
        return map;
    }

    public String getTitle() { return title; }
    public String getUrl() { return url; }
    public String getDescription() { return description; }
    public String getPicurl() { return picurl; }

    public static class Builder {
        private final String title;
        private final String url;
        private String description;
        private String picurl;

        private Builder(String title, String url) {
            this.title = title;
            this.url = url;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder picurl(String picurl) {
            this.picurl = picurl;
            return this;
        }

        public NewsArticle build() {
            return new NewsArticle(this);
        }
    }
}
