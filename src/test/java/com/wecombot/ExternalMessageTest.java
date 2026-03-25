package com.wecombot;

import com.wecombot.external.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 外部群发消息类型序列化单元测试。
 */
class ExternalMessageTest {

    // ── ExternalImageMessage ──

    @Test
    void imageMessage_fromMediaId() {
        ExternalImageMessage msg = ExternalImageMessage.ofMediaId("mid_001");
        assertEquals("image", msg.getMsgType());
        Map<String, Object> att = msg.toAttachment();
        assertEquals("image", att.get("msgtype"));
        @SuppressWarnings("unchecked")
        Map<String, Object> image = (Map<String, Object>) att.get("image");
        assertEquals("mid_001", image.get("media_id"));
    }

    @Test
    void imageMessage_fromPicUrl() {
        ExternalImageMessage msg = ExternalImageMessage.ofPicUrl("https://example.com/pic.jpg");
        Map<String, Object> att = msg.toAttachment();
        @SuppressWarnings("unchecked")
        Map<String, Object> image = (Map<String, Object>) att.get("image");
        assertEquals("https://example.com/pic.jpg", image.get("pic_url"));
    }

    // ── ExternalLinkMessage ──

    @Test
    void linkMessage_minimal() {
        ExternalLinkMessage msg = ExternalLinkMessage.builder("Title", "https://example.com").build();
        assertEquals("link", msg.getMsgType());
        Map<String, Object> att = msg.toAttachment();
        @SuppressWarnings("unchecked")
        Map<String, Object> link = (Map<String, Object>) att.get("link");
        assertEquals("Title", link.get("title"));
        assertEquals("https://example.com", link.get("url"));
        assertFalse(link.containsKey("picurl"));
        assertFalse(link.containsKey("desc"));
    }

    @Test
    void linkMessage_full() {
        ExternalLinkMessage msg = ExternalLinkMessage.builder("T", "https://example.com")
                .desc("Desc")
                .picurl("https://example.com/pic.jpg")
                .build();
        Map<String, Object> att = msg.toAttachment();
        @SuppressWarnings("unchecked")
        Map<String, Object> link = (Map<String, Object>) att.get("link");
        assertEquals("Desc", link.get("desc"));
        assertEquals("https://example.com/pic.jpg", link.get("picurl"));
    }

    // ── ExternalMiniprogramMessage ──

    @Test
    void miniprogramMessage_basic() {
        ExternalMiniprogramMessage msg = ExternalMiniprogramMessage
                .builder("Title", "wx123", "/pages/index", "pic_mid")
                .build();
        assertEquals("miniprogram", msg.getMsgType());
        Map<String, Object> att = msg.toAttachment();
        @SuppressWarnings("unchecked")
        Map<String, Object> mini = (Map<String, Object>) att.get("miniprogram");
        assertEquals("Title", mini.get("title"));
        assertEquals("wx123", mini.get("appid"));
        assertEquals("/pages/index", mini.get("page"));
        assertEquals("pic_mid", mini.get("pic_media_id"));
    }

    // ── ExternalGroupTarget ──

    @Test
    void target_toCustomers() {
        ExternalGroupTarget target = ExternalGroupTarget.toCustomers("ext_1", "ext_2");
        Map<String, Object> payload = new LinkedHashMap<>();
        target.applyTo(payload);
        assertEquals("single", payload.get("chat_type"));
        assertEquals(List.of("ext_1", "ext_2"), payload.get("external_userid"));
    }

    @Test
    void target_toCustomersWithSender() {
        ExternalGroupTarget target = ExternalGroupTarget.toCustomers(
                "emp1", List.of("ext_1"));
        Map<String, Object> payload = new LinkedHashMap<>();
        target.applyTo(payload);
        assertEquals("single", payload.get("chat_type"));
        assertEquals("emp1", payload.get("sender"));
        assertEquals(List.of("ext_1"), payload.get("external_userid"));
    }

    @Test
    void target_toCustomerGroups() {
        ExternalGroupTarget target = ExternalGroupTarget.toCustomerGroups("emp1");
        Map<String, Object> payload = new LinkedHashMap<>();
        target.applyTo(payload);
        assertEquals("group", payload.get("chat_type"));
        assertEquals("emp1", payload.get("sender"));
        assertFalse(payload.containsKey("external_userid"));
    }
}
