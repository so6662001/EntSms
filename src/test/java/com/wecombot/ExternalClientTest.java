package com.wecombot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wecombot.external.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WeComExternalClient 单元测试。
 */
class ExternalClientTest {

    private MockWebServer server;
    private WeComExternalClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        String apiBase = server.url("/cgi-bin").toString();
        client = new WeComExternalClient("test-corp", "test-external-secret", apiBase, 5);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    private void enqueueTokenResponse() {
        server.enqueue(new MockResponse()
                .setBody("{\"errcode\":0,\"errmsg\":\"ok\",\"access_token\":\"ext-token-123\",\"expires_in\":7200}")
                .addHeader("Content-Type", "application/json"));
    }

    private void enqueueSendSuccess() {
        server.enqueue(new MockResponse()
                .setBody("{\"errcode\":0,\"errmsg\":\"ok\",\"msgid\":\"msg_ext_001\",\"fail_list\":[]}")
                .addHeader("Content-Type", "application/json"));
    }

    @Test
    void sendText_toCustomers() throws Exception {
        enqueueTokenResponse();
        enqueueSendSuccess();

        Map<String, Object> result = client.sendText(
                ExternalGroupTarget.toCustomers("emp1", List.of("ext_1", "ext_2")),
                "hello external"
        );
        assertEquals(0, ((Number) result.get("errcode")).intValue());
        assertEquals("msg_ext_001", result.get("msgid"));

        RecordedRequest tokenReq = server.takeRequest();
        assertTrue(tokenReq.getPath().contains("/gettoken"));

        RecordedRequest sendReq = server.takeRequest();
        assertTrue(sendReq.getPath().contains("/externalcontact/add_msg_template"));
        assertTrue(sendReq.getPath().contains("access_token=ext-token-123"));

        Map<String, Object> body = mapper.readValue(
                sendReq.getBody().readUtf8(), new TypeReference<>() {}
        );
        assertEquals("single", body.get("chat_type"));
        assertEquals("emp1", body.get("sender"));
        @SuppressWarnings("unchecked")
        Map<String, Object> text = (Map<String, Object>) body.get("text");
        assertEquals("hello external", text.get("content"));
    }

    @Test
    void sendText_toCustomerGroups() throws Exception {
        enqueueTokenResponse();
        enqueueSendSuccess();

        Map<String, Object> result = client.sendText(
                ExternalGroupTarget.toCustomerGroups("emp1"),
                "群消息"
        );
        assertEquals(0, ((Number) result.get("errcode")).intValue());

        server.takeRequest();
        RecordedRequest sendReq = server.takeRequest();
        Map<String, Object> body = mapper.readValue(
                sendReq.getBody().readUtf8(), new TypeReference<>() {}
        );
        assertEquals("group", body.get("chat_type"));
        assertEquals("emp1", body.get("sender"));
    }

    @Test
    void sendTextWithLink() throws Exception {
        enqueueTokenResponse();
        enqueueSendSuccess();

        Map<String, Object> result = client.send(
                ExternalGroupTarget.toCustomers("emp1", List.of("ext_1")),
                "推荐给你：",
                ExternalLinkMessage.builder("新品", "https://example.com")
                        .desc("限时优惠")
                        .build()
        );
        assertEquals(0, ((Number) result.get("errcode")).intValue());

        server.takeRequest();
        RecordedRequest sendReq = server.takeRequest();
        Map<String, Object> body = mapper.readValue(
                sendReq.getBody().readUtf8(), new TypeReference<>() {}
        );
        assertNotNull(body.get("text"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> attachments = (List<Map<String, Object>>) body.get("attachments");
        assertEquals(1, attachments.size());
        assertEquals("link", attachments.get(0).get("msgtype"));
    }

    @Test
    void sendImage_only() throws Exception {
        enqueueTokenResponse();
        enqueueSendSuccess();

        client.send(
                ExternalGroupTarget.toCustomerGroups("emp1"),
                ExternalImageMessage.ofMediaId("mid_img")
        );

        server.takeRequest();
        RecordedRequest sendReq = server.takeRequest();
        Map<String, Object> body = mapper.readValue(
                sendReq.getBody().readUtf8(), new TypeReference<>() {}
        );
        assertFalse(body.containsKey("text"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> attachments = (List<Map<String, Object>>) body.get("attachments");
        assertEquals(1, attachments.size());
        assertEquals("image", attachments.get(0).get("msgtype"));
    }

    @Test
    void sendMiniprogram() throws Exception {
        enqueueTokenResponse();
        enqueueSendSuccess();

        client.send(
                ExternalGroupTarget.toCustomers("emp1", List.of("ext_1")),
                "查看小程序：",
                ExternalMiniprogramMessage.builder("优惠券", "wx123", "/pages/coupon", "pic_mid")
                        .build()
        );

        server.takeRequest();
        RecordedRequest sendReq = server.takeRequest();
        Map<String, Object> body = mapper.readValue(
                sendReq.getBody().readUtf8(), new TypeReference<>() {}
        );
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> attachments = (List<Map<String, Object>>) body.get("attachments");
        assertEquals("miniprogram", attachments.get(0).get("msgtype"));
    }

    @Test
    void tokenError_throws() {
        server.enqueue(new MockResponse()
                .setBody("{\"errcode\":40001,\"errmsg\":\"invalid credential\"}")
                .addHeader("Content-Type", "application/json"));

        WeComExternalException ex = assertThrows(WeComExternalException.class,
                () -> client.sendText(ExternalGroupTarget.toCustomerGroups("emp1"), "fail"));
        assertEquals(40001, ex.getErrcode());
    }

    @Test
    void sendError_throws() {
        enqueueTokenResponse();
        server.enqueue(new MockResponse()
                .setBody("{\"errcode\":41063,\"errmsg\":\"no external contact permission\"}")
                .addHeader("Content-Type", "application/json"));

        WeComExternalException ex = assertThrows(WeComExternalException.class,
                () -> client.sendText(ExternalGroupTarget.toCustomerGroups("emp1"), "fail"));
        assertEquals(41063, ex.getErrcode());
    }

    @Test
    void httpError_throws() {
        enqueueTokenResponse();
        server.enqueue(new MockResponse().setResponseCode(500).setBody("Server Error"));

        assertThrows(WeComExternalException.class,
                () -> client.sendText(ExternalGroupTarget.toCustomerGroups("emp1"), "fail"));
    }

    @Test
    void accessToken_cached() throws Exception {
        enqueueTokenResponse();
        enqueueSendSuccess();
        enqueueSendSuccess();

        client.sendText(ExternalGroupTarget.toCustomerGroups("emp1"), "msg1");
        client.sendText(ExternalGroupTarget.toCustomerGroups("emp1"), "msg2");

        assertEquals(3, server.getRequestCount());
    }

    @Test
    void getGroupMsgResult() throws Exception {
        enqueueTokenResponse();
        server.enqueue(new MockResponse()
                .setBody("{\"errcode\":0,\"errmsg\":\"ok\",\"send_list\":[{\"external_userid\":\"ext_1\",\"status\":1}],\"next_cursor\":\"\"}")
                .addHeader("Content-Type", "application/json"));

        Map<String, Object> result = client.getGroupMsgResult("msg_001", "", 100);
        assertEquals(0, ((Number) result.get("errcode")).intValue());
        assertNotNull(result.get("send_list"));

        server.takeRequest();
        RecordedRequest req = server.takeRequest();
        assertTrue(req.getPath().contains("/externalcontact/get_groupmsg_send_result"));
    }
}
