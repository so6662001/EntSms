package com.wecombot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wecombot.app.*;
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
 * WeComAppClient 单元测试，使用 MockWebServer 模拟 API。
 */
class AppClientTest {

    private MockWebServer server;
    private WeComAppClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        String apiBase = server.url("/cgi-bin").toString();
        client = new WeComAppClient("test-corp", "test-secret", 1000002, apiBase, 5);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    private void enqueueTokenResponse() {
        server.enqueue(new MockResponse()
                .setBody("{\"errcode\":0,\"errmsg\":\"ok\",\"access_token\":\"mock-token-123\",\"expires_in\":7200}")
                .addHeader("Content-Type", "application/json"));
    }

    private void enqueueSendSuccess() {
        server.enqueue(new MockResponse()
                .setBody("{\"errcode\":0,\"errmsg\":\"ok\",\"msgid\":\"msg_001\"}")
                .addHeader("Content-Type", "application/json"));
    }

    @Test
    void sendText_success() throws Exception {
        enqueueTokenResponse();
        enqueueSendSuccess();

        Map<String, Object> result = client.sendText(
                SendTarget.toUser("zhangsan"), "hello"
        );
        assertEquals(0, ((Number) result.get("errcode")).intValue());

        // 第一个请求是获取 token
        RecordedRequest tokenReq = server.takeRequest();
        assertTrue(tokenReq.getPath().contains("/gettoken"));
        assertTrue(tokenReq.getPath().contains("corpid=test-corp"));

        // 第二个请求是发送消息
        RecordedRequest sendReq = server.takeRequest();
        assertTrue(sendReq.getPath().contains("/message/send"));
        assertTrue(sendReq.getPath().contains("access_token=mock-token-123"));

        Map<String, Object> body = mapper.readValue(
                sendReq.getBody().readUtf8(), new TypeReference<>() {}
        );
        assertEquals("text", body.get("msgtype"));
        assertEquals("zhangsan", body.get("touser"));
        assertEquals(1000002, ((Number) body.get("agentid")).intValue());
        @SuppressWarnings("unchecked")
        Map<String, Object> text = (Map<String, Object>) body.get("text");
        assertEquals("hello", text.get("content"));
    }

    @Test
    void sendMarkdown_success() throws Exception {
        enqueueTokenResponse();
        enqueueSendSuccess();

        Map<String, Object> result = client.sendMarkdown(
                SendTarget.toUser("lisi"), "# Title"
        );
        assertEquals(0, ((Number) result.get("errcode")).intValue());

        server.takeRequest(); // skip token request
        RecordedRequest sendReq = server.takeRequest();
        Map<String, Object> body = mapper.readValue(
                sendReq.getBody().readUtf8(), new TypeReference<>() {}
        );
        assertEquals("markdown", body.get("msgtype"));
        assertEquals("lisi", body.get("touser"));
    }

    @Test
    void sendTextCard_success() throws Exception {
        enqueueTokenResponse();
        enqueueSendSuccess();

        Map<String, Object> result = client.sendTextCard(
                SendTarget.toUser("zhangsan"),
                "审批通知", "你有待审批", "https://example.com"
        );
        assertEquals(0, ((Number) result.get("errcode")).intValue());

        server.takeRequest();
        RecordedRequest sendReq = server.takeRequest();
        Map<String, Object> body = mapper.readValue(
                sendReq.getBody().readUtf8(), new TypeReference<>() {}
        );
        assertEquals("textcard", body.get("msgtype"));
        @SuppressWarnings("unchecked")
        Map<String, Object> card = (Map<String, Object>) body.get("textcard");
        assertEquals("审批通知", card.get("title"));
    }

    @Test
    void sendNews_success() throws Exception {
        enqueueTokenResponse();
        enqueueSendSuccess();

        Map<String, Object> result = client.send(
                SendTarget.toUser("zhangsan"),
                AppNewsMessage.of(
                        AppNewsArticle.builder("T", "https://example.com").build()
                )
        );
        assertEquals(0, ((Number) result.get("errcode")).intValue());

        server.takeRequest();
        RecordedRequest sendReq = server.takeRequest();
        Map<String, Object> body = mapper.readValue(
                sendReq.getBody().readUtf8(), new TypeReference<>() {}
        );
        assertEquals("news", body.get("msgtype"));
    }

    @Test
    void send_toMultipleUsers() throws Exception {
        enqueueTokenResponse();
        enqueueSendSuccess();

        client.sendText(SendTarget.toUser("a", "b", "c"), "hi");

        server.takeRequest();
        RecordedRequest sendReq = server.takeRequest();
        Map<String, Object> body = mapper.readValue(
                sendReq.getBody().readUtf8(), new TypeReference<>() {}
        );
        assertEquals("a|b|c", body.get("touser"));
    }

    @Test
    void send_toParty() throws Exception {
        enqueueTokenResponse();
        enqueueSendSuccess();

        client.sendText(SendTarget.toParty("1"), "dept msg");

        server.takeRequest();
        RecordedRequest sendReq = server.takeRequest();
        Map<String, Object> body = mapper.readValue(
                sendReq.getBody().readUtf8(), new TypeReference<>() {}
        );
        assertEquals("1", body.get("toparty"));
        assertFalse(body.containsKey("touser"));
    }

    @Test
    void accessToken_cached() throws Exception {
        enqueueTokenResponse();
        enqueueSendSuccess();
        enqueueSendSuccess();

        client.sendText(SendTarget.toUser("a"), "msg1");
        client.sendText(SendTarget.toUser("b"), "msg2");

        // 应该只请求了一次 token + 两次发送 = 3 个请求
        assertEquals(3, server.getRequestCount());
    }

    @Test
    void tokenError_throws() {
        server.enqueue(new MockResponse()
                .setBody("{\"errcode\":40013,\"errmsg\":\"invalid corpid\"}")
                .addHeader("Content-Type", "application/json"));

        WeComAppException ex = assertThrows(WeComAppException.class,
                () -> client.sendText(SendTarget.toUser("a"), "fail"));
        assertEquals(40013, ex.getErrcode());
        assertTrue(ex.getMessage().contains("invalid corpid"));
    }

    @Test
    void sendError_throws() {
        enqueueTokenResponse();
        server.enqueue(new MockResponse()
                .setBody("{\"errcode\":60011,\"errmsg\":\"no privilege to access\"}")
                .addHeader("Content-Type", "application/json"));

        WeComAppException ex = assertThrows(WeComAppException.class,
                () -> client.sendText(SendTarget.toUser("a"), "fail"));
        assertEquals(60011, ex.getErrcode());
    }

    @Test
    void httpError_throws() {
        enqueueTokenResponse();
        server.enqueue(new MockResponse().setResponseCode(500).setBody("Server Error"));

        assertThrows(WeComAppException.class,
                () -> client.sendText(SendTarget.toUser("a"), "fail"));
    }

    @Test
    void clearTokenCache_refetchesToken() throws Exception {
        enqueueTokenResponse();
        enqueueSendSuccess();
        enqueueTokenResponse();
        enqueueSendSuccess();

        client.sendText(SendTarget.toUser("a"), "msg1");
        client.clearTokenCache();
        client.sendText(SendTarget.toUser("b"), "msg2");

        // 两次 token + 两次发送 = 4 个请求
        assertEquals(4, server.getRequestCount());
    }
}
