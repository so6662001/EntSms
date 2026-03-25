package com.wecombot.app;

import java.util.Map;

/**
 * 企业微信应用消息接口。
 *
 * <p>与群机器人 {@link com.wecombot.message.WeComMessage} 不同，
 * 应用消息需要指定接收人、应用 agentId 等信息，
 * 消息体本身只负责生成 msgtype 和对应的消息内容字段。
 */
public interface AppMessage {

    /**
     * 消息类型，如 "text"、"markdown"、"textcard"、"news" 等。
     */
    String getMsgType();

    /**
     * 消息体内容，key 为 msgtype 对应的字段名，value 为内容 Map。
     * 例如 text 消息返回 {"content": "..."}
     */
    Map<String, Object> getBody();
}
