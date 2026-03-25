package com.wecombot.app;

import java.util.Map;
import java.util.Objects;

/**
 * 应用文本消息。
 *
 * <p>内容最长 2048 字节。
 */
public class AppTextMessage implements AppMessage {

    private final String content;

    public AppTextMessage(String content) {
        this.content = Objects.requireNonNull(content, "content 不能为空");
    }

    public static AppTextMessage of(String content) {
        return new AppTextMessage(content);
    }

    @Override
    public String getMsgType() {
        return "text";
    }

    @Override
    public Map<String, Object> getBody() {
        return Map.of("content", content);
    }

    public String getContent() { return content; }
}
