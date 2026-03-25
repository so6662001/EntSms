package com.wecombot;

import com.wecombot.message.*;
import org.junit.jupiter.api.Test;

import java.security.MessageDigest;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 消息类型序列化单元测试。
 */
class MessageTest {

    // ── TextMessage ──

    @Test
    void textMessage_basic() {
        TextMessage msg = TextMessage.of("hello");
        Map<String, Object> payload = msg.toPayload();

        assertEquals("text", payload.get("msgtype"));
        @SuppressWarnings("unchecked")
        Map<String, Object> text = (Map<String, Object>) payload.get("text");
        assertEquals("hello", text.get("content"));
        assertFalse(text.containsKey("mentioned_list"));
    }

    @Test
    void textMessage_withMentions() {
        TextMessage msg = TextMessage.builder("hi")
                .mentionedList("user1", "@all")
                .mentionedMobileList("13800138000")
                .build();
        Map<String, Object> payload = msg.toPayload();

        @SuppressWarnings("unchecked")
        Map<String, Object> text = (Map<String, Object>) payload.get("text");
        assertEquals(List.of("user1", "@all"), text.get("mentioned_list"));
        assertEquals(List.of("13800138000"), text.get("mentioned_mobile_list"));
    }

    // ── MarkdownMessage ──

    @Test
    void markdownMessage_basic() {
        MarkdownMessage msg = MarkdownMessage.of("# Title\nbody");
        Map<String, Object> payload = msg.toPayload();

        assertEquals("markdown", payload.get("msgtype"));
        @SuppressWarnings("unchecked")
        Map<String, Object> md = (Map<String, Object>) payload.get("markdown");
        assertTrue(((String) md.get("content")).contains("# Title"));
    }

    // ── ImageMessage ──

    @Test
    void imageMessage_fromBytes() throws Exception {
        byte[] data = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x00, 0x01, 0x02};
        ImageMessage msg = ImageMessage.fromBytes(data);
        Map<String, Object> payload = msg.toPayload();

        assertEquals("image", payload.get("msgtype"));
        @SuppressWarnings("unchecked")
        Map<String, Object> image = (Map<String, Object>) payload.get("image");
        assertNotNull(image.get("base64"));
        assertFalse(((String) image.get("base64")).isEmpty());

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] hash = md5.digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        assertEquals(sb.toString(), image.get("md5"));
    }

    // ── NewsMessage ──

    @Test
    void newsMessage_singleArticle() {
        NewsArticle article = NewsArticle.builder("Title", "https://example.com").build();
        NewsMessage msg = NewsMessage.of(article);
        Map<String, Object> payload = msg.toPayload();

        assertEquals("news", payload.get("msgtype"));
        @SuppressWarnings("unchecked")
        Map<String, Object> news = (Map<String, Object>) payload.get("news");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> articles = (List<Map<String, Object>>) news.get("articles");
        assertEquals(1, articles.size());
        assertEquals("Title", articles.get(0).get("title"));
    }

    @Test
    void newsArticle_withOptionalFields() {
        NewsArticle article = NewsArticle.builder("T", "https://example.com")
                .description("Desc")
                .picurl("https://example.com/pic.png")
                .build();
        Map<String, Object> map = article.toMap();
        assertEquals("Desc", map.get("description"));
        assertEquals("https://example.com/pic.png", map.get("picurl"));
    }

    @Test
    void newsMessage_maxArticles() {
        List<NewsArticle> articles = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            articles.add(NewsArticle.builder("A" + i, "https://example.com").build());
        }
        NewsMessage msg = new NewsMessage(articles);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>)
                ((Map<String, Object>) msg.toPayload().get("news")).get("articles");
        assertEquals(8, list.size());
    }

    @Test
    void newsMessage_tooManyArticles_throws() {
        List<NewsArticle> articles = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            articles.add(NewsArticle.builder("A" + i, "https://example.com").build());
        }
        assertThrows(IllegalArgumentException.class, () -> new NewsMessage(articles));
    }

    // ── FileMessage ──

    @Test
    void fileMessage_basic() {
        FileMessage msg = FileMessage.of("mid_001");
        Map<String, Object> payload = msg.toPayload();

        assertEquals("file", payload.get("msgtype"));
        @SuppressWarnings("unchecked")
        Map<String, Object> file = (Map<String, Object>) payload.get("file");
        assertEquals("mid_001", file.get("media_id"));
    }

    // ── TemplateCardMessage ──

    @Test
    void templateCardMessage_minimal() {
        TemplateCardMessage card = TemplateCardMessage
                .builder("Alert", "https://example.com")
                .build();
        Map<String, Object> payload = card.toPayload();

        assertEquals("template_card", payload.get("msgtype"));
        @SuppressWarnings("unchecked")
        Map<String, Object> tc = (Map<String, Object>) payload.get("template_card");
        assertEquals("text_notice", tc.get("card_type"));
        @SuppressWarnings("unchecked")
        Map<String, Object> mainTitle = (Map<String, Object>) tc.get("main_title");
        assertEquals("Alert", mainTitle.get("title"));
    }

    @Test
    void templateCardMessage_full() {
        TemplateCardMessage card = TemplateCardMessage
                .builder("Alert", "https://example.com")
                .sourceDesc("Monitor")
                .subTitleText("Sub")
                .emphasisContentTitle("99")
                .emphasisContentDesc("pending")
                .horizontalContentList(List.of(Map.of("keyname", "K", "value", "V")))
                .jumpList(List.of(Map.of("type", "1", "title", "Go", "url", "https://example.com")))
                .build();
        Map<String, Object> payload = card.toPayload();

        @SuppressWarnings("unchecked")
        Map<String, Object> tc = (Map<String, Object>) payload.get("template_card");
        @SuppressWarnings("unchecked")
        Map<String, Object> source = (Map<String, Object>) tc.get("source");
        assertEquals("Monitor", source.get("desc"));
        @SuppressWarnings("unchecked")
        Map<String, Object> emphasis = (Map<String, Object>) tc.get("emphasis_content");
        assertEquals("99", emphasis.get("title"));
        @SuppressWarnings("unchecked")
        List<?> hlist = (List<?>) tc.get("horizontal_content_list");
        assertEquals(1, hlist.size());
        @SuppressWarnings("unchecked")
        List<?> jlist = (List<?>) tc.get("jump_list");
        assertEquals(1, jlist.size());
    }
}
