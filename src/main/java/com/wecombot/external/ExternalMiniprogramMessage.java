package com.wecombot.external;

import java.util.*;

/**
 * 外部群发小程序附件。
 */
public class ExternalMiniprogramMessage implements ExternalMessage {

    private final String title;
    private final String appid;
    private final String page;
    private final String picMediaId;

    private ExternalMiniprogramMessage(Builder builder) {
        this.title = Objects.requireNonNull(builder.title, "title 不能为空");
        this.appid = Objects.requireNonNull(builder.appid, "appid 不能为空");
        this.page = Objects.requireNonNull(builder.page, "page 不能为空");
        this.picMediaId = Objects.requireNonNull(builder.picMediaId, "picMediaId 不能为空");
    }

    public static Builder builder(String title, String appid, String page, String picMediaId) {
        return new Builder(title, appid, page, picMediaId);
    }

    @Override
    public String getMsgType() {
        return "miniprogram";
    }

    @Override
    public Map<String, Object> toAttachment() {
        Map<String, Object> mini = new LinkedHashMap<>();
        mini.put("title", title);
        mini.put("pic_media_id", picMediaId);
        mini.put("appid", appid);
        mini.put("page", page);

        Map<String, Object> attachment = new LinkedHashMap<>();
        attachment.put("msgtype", "miniprogram");
        attachment.put("miniprogram", mini);
        return attachment;
    }

    public static class Builder {
        private final String title;
        private final String appid;
        private final String page;
        private final String picMediaId;

        private Builder(String title, String appid, String page, String picMediaId) {
            this.title = title;
            this.appid = appid;
            this.page = page;
            this.picMediaId = picMediaId;
        }

        public ExternalMiniprogramMessage build() { return new ExternalMiniprogramMessage(this); }
    }
}
