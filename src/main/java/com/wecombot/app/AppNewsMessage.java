package com.wecombot.app;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 应用图文消息，最多支持 8 条图文。
 */
public class AppNewsMessage implements AppMessage {

    private final List<AppNewsArticle> articles;

    public AppNewsMessage(List<AppNewsArticle> articles) {
        Objects.requireNonNull(articles, "articles 不能为空");
        if (articles.isEmpty()) {
            throw new IllegalArgumentException("articles 不能为空列表");
        }
        if (articles.size() > 8) {
            throw new IllegalArgumentException("图文消息最多支持 8 条");
        }
        this.articles = Collections.unmodifiableList(new ArrayList<>(articles));
    }

    public static AppNewsMessage of(AppNewsArticle... articles) {
        return new AppNewsMessage(Arrays.asList(articles));
    }

    @Override
    public String getMsgType() {
        return "news";
    }

    @Override
    public Map<String, Object> getBody() {
        List<Map<String, Object>> articleMaps = articles.stream()
                .map(AppNewsArticle::toMap)
                .collect(Collectors.toList());
        return Map.of("articles", articleMaps);
    }

    public List<AppNewsArticle> getArticles() { return articles; }
}
