package com.wecombot.app;

import java.util.Map;
import java.util.Objects;

/**
 * 应用 Markdown 消息。
 *
 * <p>内容最长 2048 字节。支持企业微信 Markdown 语法子集。
 * 注意：仅企业微信内部可见，不支持外部联系人。
 */
public class AppMarkdownMessage implements AppMessage {

    private final String content;

    public AppMarkdownMessage(String content) {
        this.content = Objects.requireNonNull(content, "content 不能为空");
    }

    public static AppMarkdownMessage of(String content) {
        return new AppMarkdownMessage(content);
    }

    @Override
    public String getMsgType() {
        return "markdown";
    }

    @Override
    public Map<String, Object> getBody() {
        return Map.of("content", content);
    }

    public String getContent() { return content; }
}
