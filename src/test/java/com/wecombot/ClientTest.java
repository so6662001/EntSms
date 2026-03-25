package com.wecombot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wecombot.client.WeComBotClient;
import com.wecombot.client.WeComBotException;
import com.wecombot.message.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WeComBotClient 单元测试，使用 MockWebServer 模拟 API。
 */
class ClientTest {

    private MockWebServer server;
    private WeComBotClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        String baseUrl = server.url("/cgi-bin/webhook/send").toString();
        // 去掉 query 部分，因为 client 会自己拼接
        baseUrl = baseUrl.replace("?", "");
        client = new WeComBotClient("test-key", baseUrl, 5);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void emptyKey_throws() {
        assertThrows(IllegalArgumentException.class, () -> new WeComBotClient(""));
    }

    @Test
    void sendText_success() throws Exception {
        server.enqueue(new MockResponse()
                .setBody("{\"errcode\":0,\"errmsg\":\"ok\"}")
                .addHeader("Content-Type", "application/json"));

        Map<String, Object> result = client.sendText("hello");
        assertEquals(0, ((Number) result.get("errcode")).intValue());

        RecordedRequest request = server.takeRequest();
        assertEquals("POST", request.getMethod());
        Map<String, Object> body = mapper.readValue(
                request.getBody().readUtf8(),
                new TypeReference<>() {}
        );
        assertEquals("text", body.get("msgtype"));
        @SuppressWarnings("unchecked")
        Map<String, Object> text = (Map<String, Object>) body.get("text");
        assertEquals("hello", text.get("content"));
    }

    @Test
    void sendMarkdown_success() throws Exception {
        server.enqueue(new MockResponse()
                .setBody("{\"errcode\":0,\"errmsg\":\"ok\"}")
                .addHeader("Content-Type", "application/json"));

        Map<String, Object> result = client.sendMarkdown("# Title");
        assertEquals(0, ((Number) result.get("errcode")).intValue());

        RecordedRequest request = server.takeRequest();
        Map<String, Object> body = mapper.readValue(
                request.getBody().readUtf8(),
                new TypeReference<>() {}
        );
        assertEquals("markdown", body.get("msgtype"));
    }

    @Test
    void sendNews_success() throws Exception {
        server.enqueue(new MockResponse()
                .setBody("{\"errcode\":0,\"errmsg\":\"ok\"}")
                .addHeader("Content-Type", "application/json"));

        NewsMessage news = NewsMessage.of(
                NewsArticle.builder("T", "https://example.com").build()
        );
        Map<String, Object> result = client.send(news);
        assertEquals(0, ((Number) result.get("errcode")).intValue());

        RecordedRequest request = server.takeRequest();
        Map<String, Object> body = mapper.readValue(
                request.getBody().readUtf8(),
                new TypeReference<>() {}
        );
        assertEquals("news", body.get("msgtype"));
    }

    @Test
    void sendTemplateCard_success() throws Exception {
        server.enqueue(new MockResponse()
                .setBody("{\"errcode\":0,\"errmsg\":\"ok\"}")
                .addHeader("Content-Type", "application/json"));

        TemplateCardMessage card = TemplateCardMessage
                .builder("Test", "https://example.com")
                .build();
        Map<String, Object> result = client.send(card);
        assertEquals(0, ((Number) result.get("errcode")).intValue());

        RecordedRequest request = server.takeRequest();
        Map<String, Object> body = mapper.readValue(
                request.getBody().readUtf8(),
                new TypeReference<>() {}
        );
        assertEquals("template_card", body.get("msgtype"));
    }

    @Test
    void apiError_throwsWeComBotException() {
        server.enqueue(new MockResponse()
                .setBody("{\"errcode\":93000,\"errmsg\":\"invalid webhook url\"}")
                .addHeader("Content-Type", "application/json"));

        WeComBotException ex = assertThrows(WeComBotException.class,
                () -> client.sendText("fail"));
        assertEquals(93000, ex.getErrcode());
        assertTrue(ex.getMessage().contains("invalid webhook url"));
    }

    @Test
    void httpError_throwsWeComBotException() {
        server.enqueue(new MockResponse().setResponseCode(500).setBody("Server Error"));

        assertThrows(WeComBotException.class, () -> client.sendText("fail"));
    }

    @Test
    void sendImage_success() throws Exception {
        server.enqueue(new MockResponse()
                .setBody("{\"errcode\":0,\"errmsg\":\"ok\"}")
                .addHeader("Content-Type", "application/json"));

        byte[] imageData = {(byte) 0x89, 0x50, 0x4E, 0x47};
        Map<String, Object> result = client.sendImage(imageData);
        assertEquals(0, ((Number) result.get("errcode")).intValue());

        RecordedRequest request = server.takeRequest();
        Map<String, Object> body = mapper.readValue(
                request.getBody().readUtf8(),
                new TypeReference<>() {}
        );
        assertEquals("image", body.get("msgtype"));
    }
}
