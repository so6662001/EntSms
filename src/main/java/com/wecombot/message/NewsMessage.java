package com.wecombot.message;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 图文消息，最多支持 8 条图文。
 */
public class NewsMessage implements WeComMessage {

    private final List<NewsArticle> articles;

    public NewsMessage(List<NewsArticle> articles) {
        Objects.requireNonNull(articles, "articles 不能为空");
        if (articles.isEmpty()) {
            throw new IllegalArgumentException("articles 不能为空列表");
        }
        if (articles.size() > 8) {
            throw new IllegalArgumentException("图文消息最多支持 8 条");
        }
        this.articles = Collections.unmodifiableList(new ArrayList<>(articles));
    }

    public static NewsMessage of(NewsArticle... articles) {
        return new NewsMessage(Arrays.asList(articles));
    }

    @Override
    public Map<String, Object> toPayload() {
        List<Map<String, Object>> articleMaps = articles.stream()
                .map(NewsArticle::toMap)
                .collect(Collectors.toList());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("msgtype", "news");
        payload.put("news", Map.of("articles", articleMaps));
        return payload;
    }

    public List<NewsArticle> getArticles() { return articles; }
}
