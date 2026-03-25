package com.wecombot.app;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 企业微信应用消息客户端。
 *
 * <p>通过企业微信应用消息 API 向指定用户/部门/标签定向推送消息。
 * 需要企业的 corpId、应用的 corpSecret 和 agentId。
 *
 * <p>access_token 自动获取并缓存，过期前自动刷新。
 *
 * <pre>{@code
 * WeComAppClient appClient = new WeComAppClient("corpId", "corpSecret", 1000002);
 *
 * // 给指定用户发文本消息
 * appClient.sendText(SendTarget.toUser("zhangsan"), "你好，这是一条私信");
 *
 * // 给指定用户发 Markdown
 * appClient.sendMarkdown(SendTarget.toUser("lisi"), "# 审批通知\n你有一条待审批");
 *
 * // 给指定用户发文本卡片
 * appClient.send(SendTarget.toUser("zhangsan"), AppTextCardMessage
 *     .builder("审批通知", "你有一条待审批的报销单", "https://example.com/approve/123")
 *     .btnTxt("去审批")
 *     .build());
 * }</pre>
 *
 * <h3>前置条件</h3>
 * <ol>
 *   <li>登录 <a href="https://work.weixin.qq.com">企业微信管理后台</a></li>
 *   <li>获取 corpId（企业信息 → 企业ID）</li>
 *   <li>创建自建应用，获取 agentId 和 Secret</li>
 *   <li>在应用可见范围中添加目标用户/部门</li>
 * </ol>
 *
 * @see <a href="https://developer.work.weixin.qq.com/document/path/90236">企业微信应用消息 API</a>
 */
public class WeComAppClient {

    private static final Logger log = LoggerFactory.getLogger(WeComAppClient.class);
    private static final String DEFAULT_API_BASE = "https://qyapi.weixin.qq.com/cgi-bin";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private final String corpId;
    private final String corpSecret;
    private final int agentId;
    private final String apiBase;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    private volatile AccessToken cachedToken;

    /**
     * 使用默认配置创建客户端。
     *
     * @param corpId     企业 ID
     * @param corpSecret 应用的 Secret
     * @param agentId    应用的 AgentId
     */
    public WeComAppClient(String corpId, String corpSecret, int agentId) {
        this(corpId, corpSecret, agentId, DEFAULT_API_BASE, 10);
    }

    /**
     * 创建客户端（完整参数）。
     *
     * @param corpId     企业 ID
     * @param corpSecret 应用的 Secret
     * @param agentId    应用的 AgentId
     * @param apiBase    API 基础 URL
     * @param timeoutSec 请求超时秒数
     */
    public WeComAppClient(String corpId, String corpSecret, int agentId, String apiBase, int timeoutSec) {
        this.corpId = Objects.requireNonNull(corpId, "corpId 不能为空");
        this.corpSecret = Objects.requireNonNull(corpSecret, "corpSecret 不能为空");
        this.agentId = agentId;
        this.apiBase = Objects.requireNonNull(apiBase, "apiBase 不能为空");
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeoutSec, TimeUnit.SECONDS)
                .readTimeout(timeoutSec, TimeUnit.SECONDS)
                .writeTimeout(timeoutSec, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 仅用于测试：允许注入自定义 OkHttpClient。
     */
    WeComAppClient(String corpId, String corpSecret, int agentId, String apiBase, OkHttpClient httpClient) {
        this.corpId = corpId;
        this.corpSecret = corpSecret;
        this.agentId = agentId;
        this.apiBase = apiBase;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    // ── access_token 管理 ────────────────────────────────────────

    /**
     * 获取 access_token，自动缓存并在过期前刷新。
     */
    public String getAccessToken() {
        AccessToken token = cachedToken;
        if (token != null && !token.isExpired()) {
            return token.getToken();
        }
        synchronized (this) {
            token = cachedToken;
            if (token != null && !token.isExpired()) {
                return token.getToken();
            }
            cachedToken = fetchAccessToken();
            return cachedToken.getToken();
        }
    }

    private AccessToken fetchAccessToken() {
        String url = apiBase + "/gettoken?corpid=" + corpId + "&corpsecret=" + corpSecret;
        Request request = new Request.Builder().url(url).get().build();

        try (Response response = httpClient.newCall(request).execute()) {
            Map<String, Object> result = parseResponse(response);
            String tokenStr = (String) result.get("access_token");
            int expiresIn = ((Number) result.get("expires_in")).intValue();
            log.info("Access token obtained, expires_in={}s", expiresIn);
            return new AccessToken(tokenStr, expiresIn);
        } catch (WeComAppException e) {
            throw e;
        } catch (IOException e) {
            throw new WeComAppException("获取 access_token 失败", e);
        }
    }

    /** 手动清除 token 缓存，下次调用时会重新获取。 */
    public void clearTokenCache() {
        cachedToken = null;
    }

    // ── 消息发送 ─────────────────────────────────────────────────

    /**
     * 向指定目标发送应用消息。
     *
     * @param target  发送目标（用户/部门/标签）
     * @param message 消息对象
     * @return API 响应 Map
     * @throws WeComAppException API 错误或网络异常时抛出
     */
    public Map<String, Object> send(SendTarget target, AppMessage message) {
        Map<String, Object> payload = buildPayload(target, message);
        try {
            String json = objectMapper.writeValueAsString(payload);
            log.debug("Sending app message: {}", json);

            String url = apiBase + "/message/send?access_token=" + getAccessToken();
            RequestBody body = RequestBody.create(json, JSON_MEDIA_TYPE);
            Request request = new Request.Builder().url(url).post(body).build();

            try (Response response = httpClient.newCall(request).execute()) {
                Map<String, Object> result = parseResponse(response);
                log.info("App message sent: msgtype={}, target={}",
                        message.getMsgType(), target.getToUser());
                return result;
            }
        } catch (WeComAppException e) {
            throw e;
        } catch (IOException e) {
            throw new WeComAppException("发送应用消息失败", e);
        }
    }

    // ── 快捷方法 ─────────────────────────────────────────────────

    /** 给指定目标发送文本消息。 */
    public Map<String, Object> sendText(SendTarget target, String content) {
        return send(target, AppTextMessage.of(content));
    }

    /** 给指定目标发送 Markdown 消息。 */
    public Map<String, Object> sendMarkdown(SendTarget target, String content) {
        return send(target, AppMarkdownMessage.of(content));
    }

    /** 给指定目标发送文本卡片消息。 */
    public Map<String, Object> sendTextCard(SendTarget target,
                                             String title, String description, String url) {
        return send(target, AppTextCardMessage.builder(title, description, url).build());
    }

    // ── 内部工具 ─────────────────────────────────────────────────

    private Map<String, Object> buildPayload(SendTarget target, AppMessage message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        target.applyTo(payload);
        payload.put("msgtype", message.getMsgType());
        payload.put("agentid", agentId);
        payload.put(message.getMsgType(), message.getBody());
        return payload;
    }

    private Map<String, Object> parseResponse(Response response) throws IOException {
        if (!response.isSuccessful()) {
            throw new WeComAppException(-1, "HTTP " + response.code() + ": " + response.message());
        }

        String responseBody = response.body() != null ? response.body().string() : "{}";
        Map<String, Object> result = objectMapper.readValue(
                responseBody, new TypeReference<>() {}
        );

        Object errcodeObj = result.get("errcode");
        int errcode = errcodeObj instanceof Number ? ((Number) errcodeObj).intValue() : 0;
        if (errcode != 0) {
            String errmsg = (String) result.getOrDefault("errmsg", "unknown error");
            throw new WeComAppException(errcode, errmsg);
        }
        return result;
    }
}
