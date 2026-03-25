package com.wecombot.external;

import java.util.*;

/**
 * 外部群发链接附件。
 */
public class ExternalLinkMessage implements ExternalMessage {

    private final String title;
    private final String url;
    private final String picurl;
    private final String desc;
    private final String mediaId;

    private ExternalLinkMessage(Builder builder) {
        this.title = Objects.requireNonNull(builder.title, "title 不能为空");
        this.url = Objects.requireNonNull(builder.url, "url 不能为空");
        this.picurl = builder.picurl;
        this.desc = builder.desc;
        this.mediaId = builder.mediaId;
    }

    public static Builder builder(String title, String url) {
        return new Builder(title, url);
    }

    @Override
    public String getMsgType() {
        return "link";
    }

    @Override
    public Map<String, Object> toAttachment() {
        Map<String, Object> link = new LinkedHashMap<>();
        link.put("title", title);
        link.put("url", url);
        if (picurl != null && !picurl.isEmpty()) link.put("picurl", picurl);
        if (desc != null && !desc.isEmpty()) link.put("desc", desc);
        if (mediaId != null && !mediaId.isEmpty()) link.put("media_id", mediaId);

        Map<String, Object> attachment = new LinkedHashMap<>();
        attachment.put("msgtype", "link");
        attachment.put("link", link);
        return attachment;
    }

    public static class Builder {
        private final String title;
        private final String url;
        private String picurl;
        private String desc;
        private String mediaId;

        private Builder(String title, String url) {
            this.title = title;
            this.url = url;
        }

        public Builder picurl(String picurl) { this.picurl = picurl; return this; }
        public Builder desc(String desc) { this.desc = desc; return this; }
        public Builder mediaId(String mediaId) { this.mediaId = mediaId; return this; }

        public ExternalLinkMessage build() { return new ExternalLinkMessage(this); }
    }
}
