package com.wecombot.message;

import java.util.*;

/**
 * Markdown 消息。
 *
 * <p>内容最长 4096 字节，支持企业微信 Markdown 语法子集：
 * 标题、加粗、链接、引用、字体颜色、表格等。
 */
public class MarkdownMessage implements WeComMessage {

    private final String content;

    public MarkdownMessage(String content) {
        this.content = Objects.requireNonNull(content, "content 不能为空");
    }

    public static MarkdownMessage of(String content) {
        return new MarkdownMessage(content);
    }

    @Override
    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("msgtype", "markdown");
        payload.put("markdown", Map.of("content", content));
        return payload;
    }

    public String getContent() { return content; }
}
