package com.wecombot.external;

import java.util.Map;
import java.util.Objects;

/**
 * 外部群发图片附件。
 *
 * <p>需要先通过素材管理接口上传图片获取 media_id，
 * 或者直接使用图片的 pic_url。
 */
public class ExternalImageMessage implements ExternalMessage {

    private final String mediaId;
    private final String picUrl;

    private ExternalImageMessage(String mediaId, String picUrl) {
        this.mediaId = mediaId;
        this.picUrl = picUrl;
    }

    /** 使用 media_id 构造。 */
    public static ExternalImageMessage ofMediaId(String mediaId) {
        return new ExternalImageMessage(
                Objects.requireNonNull(mediaId, "mediaId 不能为空"), null);
    }

    /** 使用图片 URL 构造。 */
    public static ExternalImageMessage ofPicUrl(String picUrl) {
        return new ExternalImageMessage(
                null, Objects.requireNonNull(picUrl, "picUrl 不能为空"));
    }

    @Override
    public String getMsgType() {
        return "image";
    }

    @Override
    public Map<String, Object> toAttachment() {
        Map<String, Object> attachment = new java.util.LinkedHashMap<>();
        attachment.put("msgtype", "image");
        Map<String, Object> image = new java.util.LinkedHashMap<>();
        if (mediaId != null) image.put("media_id", mediaId);
        if (picUrl != null) image.put("pic_url", picUrl);
        attachment.put("image", image);
        return attachment;
    }
}
