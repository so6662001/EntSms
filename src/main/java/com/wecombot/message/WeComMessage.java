package com.wecombot.message;

import java.util.Map;

/**
 * 企业微信群机器人消息接口。
 * 所有消息类型均需实现此接口，返回符合 API 规范的 payload。
 */
public interface WeComMessage {

    /**
     * 将消息转换为 API 请求的 payload Map。
     */
    Map<String, Object> toPayload();
}
