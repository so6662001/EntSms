package com.wecombot.external;

import java.util.Map;

/**
 * 外部群发消息内容接口。
 *
 * <p>外部群发消息的附件类型，支持：图片、链接、小程序。
 * 文本内容通过 text 字段直接传递，不走此接口。
 */
public interface ExternalMessage {

    /**
     * 附件类型，如 "image"、"link"、"miniprogram"。
     */
    String getMsgType();

    /**
     * 附件内容 Map。
     */
    Map<String, Object> toAttachment();
}
