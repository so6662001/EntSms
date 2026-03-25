package com.wecombot;

import com.wecombot.app.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 应用消息类型序列化单元测试。
 */
class AppMessageTest {

    @Test
    void textMessage_basic() {
        AppTextMessage msg = AppTextMessage.of("hello");
        assertEquals("text", msg.getMsgType());
        assertEquals("hello", msg.getBody().get("content"));
    }

    @Test
    void markdownMessage_basic() {
        AppMarkdownMessage msg = AppMarkdownMessage.of("# Title");
        assertEquals("markdown", msg.getMsgType());
        assertTrue(((String) msg.getBody().get("content")).contains("# Title"));
    }

    @Test
    void textCardMessage_minimal() {
        AppTextCardMessage card = AppTextCardMessage
                .builder("Title", "Desc", "https://example.com")
                .build();
        assertEquals("textcard", card.getMsgType());
        Map<String, Object> body = card.getBody();
        assertEquals("Title", body.get("title"));
        assertEquals("Desc", body.get("description"));
        assertEquals("https://example.com", body.get("url"));
        assertFalse(body.containsKey("btntxt"));
    }

    @Test
    void textCardMessage_withBtnTxt() {
        AppTextCardMessage card = AppTextCardMessage
                .builder("T", "D", "https://example.com")
                .btnTxt("去审批")
                .build();
        assertEquals("去审批", card.getBody().get("btntxt"));
    }

    @Test
    void newsMessage_singleArticle() {
        AppNewsMessage news = AppNewsMessage.of(
                AppNewsArticle.builder("Title", "https://example.com").build()
        );
        assertEquals("news", news.getMsgType());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> articles = (List<Map<String, Object>>) news.getBody().get("articles");
        assertEquals(1, articles.size());
        assertEquals("Title", articles.get(0).get("title"));
    }

    @Test
    void newsMessage_withOptionalFields() {
        AppNewsArticle article = AppNewsArticle.builder("T", "https://example.com")
                .description("Desc")
                .picurl("https://example.com/pic.png")
                .build();
        Map<String, Object> map = article.toMap();
        assertEquals("Desc", map.get("description"));
        assertEquals("https://example.com/pic.png", map.get("picurl"));
    }

    @Test
    void newsMessage_tooManyArticles_throws() {
        List<AppNewsArticle> articles = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            articles.add(AppNewsArticle.builder("A" + i, "https://example.com").build());
        }
        assertThrows(IllegalArgumentException.class, () -> new AppNewsMessage(articles));
    }

    // ── SendTarget ──

    @Test
    void sendTarget_toUser() {
        SendTarget target = SendTarget.toUser("zhangsan", "lisi");
        Map<String, Object> payload = new LinkedHashMap<>();
        target.applyTo(payload);
        assertEquals("zhangsan|lisi", payload.get("touser"));
        assertFalse(payload.containsKey("toparty"));
    }

    @Test
    void sendTarget_toParty() {
        SendTarget target = SendTarget.toParty("1", "2");
        Map<String, Object> payload = new LinkedHashMap<>();
        target.applyTo(payload);
        assertEquals("1|2", payload.get("toparty"));
    }

    @Test
    void sendTarget_toAll() {
        SendTarget target = SendTarget.toAll();
        Map<String, Object> payload = new LinkedHashMap<>();
        target.applyTo(payload);
        assertEquals("@all", payload.get("touser"));
    }

    @Test
    void sendTarget_combined() {
        SendTarget target = SendTarget.builder()
                .toUser("zhangsan")
                .toParty("1")
                .toTag("tag1")
                .build();
        Map<String, Object> payload = new LinkedHashMap<>();
        target.applyTo(payload);
        assertEquals("zhangsan", payload.get("touser"));
        assertEquals("1", payload.get("toparty"));
        assertEquals("tag1", payload.get("totag"));
    }

    @Test
    void sendTarget_builder_emptyThrows() {
        assertThrows(IllegalArgumentException.class, () -> SendTarget.builder().build());
    }
}
