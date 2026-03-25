package com.wecombot.message;

import java.util.*;

/**
 * 文件消息。
 *
 * <p>需要先通过 {@link com.wecombot.client.WeComBotClient#uploadFile} 上传文件获取 media_id。
 */
public class FileMessage implements WeComMessage {

    private final String mediaId;

    public FileMessage(String mediaId) {
        this.mediaId = Objects.requireNonNull(mediaId, "mediaId 不能为空");
    }

    public static FileMessage of(String mediaId) {
        return new FileMessage(mediaId);
    }

    @Override
    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("msgtype", "file");
        payload.put("file", Map.of("media_id", mediaId));
        return payload;
    }

    public String getMediaId() { return mediaId; }
}
