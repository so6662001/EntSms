package com.wecombot.external;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 企业微信外部客户消息客户端。
 *
 * <p>通过「客户联系 - 群发消息」API 向外部客户或客户群发送消息。
 * 这是企业微信官方推荐的向外部联系人发消息的方式。
 *
 * <h3>工作原理</h3>
 * <ul>
 *   <li>群发消息以<b>员工名义</b>发出，不是以应用或机器人的名义</li>
 *   <li>消息会出现在员工与客户的聊天窗口中</li>
 *   <li>每个客户每天最多收到 1 条群发消息（企业微信限制）</li>
 * </ul>
 *
 * <h3>发送模式</h3>
 * <ul>
 *   <li><b>客户私聊</b> (chat_type=single) — 以员工名义给外部客户发私聊消息</li>
 *   <li><b>客户群</b> (chat_type=group) — 以员工名义往客户群里发消息</li>
 * </ul>
 *
 * <h3>前置条件</h3>
 * <ol>
 *   <li>使用「客户联系」应用的 Secret（不是自建应用的 Secret）</li>
 *   <li>企业微信管理后台 → 客户联系 → API → 获取 Secret</li>
 *   <li>发送者（员工）需要在「客户联系」功能的使用范围内</li>
 *   <li>目标客户需要已添加为员工的外部联系人</li>
 * </ol>
 *
 * <pre>{@code
 * WeComExternalClient client = new WeComExternalClient("corpId", "customerContactSecret");
 *
 * // 给外部客户发私聊消息
 * client.sendText(
 *     ExternalGroupTarget.toCustomers("employee1", List.of("external_user_1", "external_user_2")),
 *     "你好，感谢关注我们的产品！"
 * );
 *
 * // 往客户群发消息
 * client.sendText(
 *     ExternalGroupTarget.toCustomerGroups("employee1"),
 *     "各位好，本周新品上线了！"
 * );
 * }</pre>
 *
 * @see <a href="https://developer.work.weixin.qq.com/document/path/92135">客户联系 - 创建企业群发</a>
 */
public class WeComExternalClient {

    private static final Logger log = LoggerFactory.getLogger(WeComExternalClient.class);
    private static final String DEFAULT_API_BASE = "https://qyapi.weixin.qq.com/cgi-bin";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private final String corpId;
    private final String corpSecret;
    private final String apiBase;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    private volatile CachedToken cachedToken;

    /**
     * 使用默认配置创建客户端。
     *
     * @param corpId     企业 ID
     * @param corpSecret 客户联系应用的 Secret
     */
    public WeComExternalClient(String corpId, String corpSecret) {
        this(corpId, corpSecret, DEFAULT_API_BASE, 10);
    }

    public WeComExternalClient(String corpId, String corpSecret, String apiBase, int timeoutSec) {
        this.corpId = Objects.requireNonNull(corpId, "corpId 不能为空");
        this.corpSecret = Objects.requireNonNull(corpSecret, "corpSecret 不能为空");
        this.apiBase = Objects.requireNonNull(apiBase, "apiBase 不能为空");
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeoutSec, TimeUnit.SECONDS)
                .readTimeout(timeoutSec, TimeUnit.SECONDS)
                .writeTimeout(timeoutSec, TimeUnit.SECONDS)
                .build();
    }

    /** 仅用于测试：允许注入自定义 OkHttpClient。 */
    WeComExternalClient(String corpId, String corpSecret, String apiBase, OkHttpClient httpClient) {
        this.corpId = corpId;
        this.corpSecret = corpSecret;
        this.apiBase = apiBase;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    // ── access_token 管理 ────────────────────────────────────────

    public String getAccessToken() {
        CachedToken token = cachedToken;
        if (token != null && !token.isExpired()) {
            return token.token;
        }
        synchronized (this) {
            token = cachedToken;
            if (token != null && !token.isExpired()) {
                return token.token;
            }
            cachedToken = fetchAccessToken();
            return cachedToken.token;
        }
    }

    private CachedToken fetchAccessToken() {
        String url = apiBase + "/gettoken?corpid=" + corpId + "&corpsecret=" + corpSecret;
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            Map<String, Object> result = parseResponse(response);
            String tokenStr = (String) result.get("access_token");
            int expiresIn = ((Number) result.get("expires_in")).intValue();
            log.info("External access token obtained, expires_in={}s", expiresIn);
            return new CachedToken(tokenStr, expiresIn);
        } catch (WeComExternalException e) {
            throw e;
        } catch (IOException e) {
            throw new WeComExternalException("获取 access_token 失败", e);
        }
    }

    public void clearTokenCache() {
        cachedToken = null;
    }

    // ── 群发消息 ─────────────────────────────────────────────────

    /**
     * 创建企业群发任务。
     *
     * <p>发送纯文本群发消息。
     *
     * @param target  发送目标
     * @param content 文本内容
     * @return API 响应（含 msgid 用于查询发送结果）
     */
    public Map<String, Object> sendText(ExternalGroupTarget target, String content) {
        return sendGroupMessage(target, content, null);
    }

    /**
     * 创建企业群发任务（文本 + 附件）。
     *
     * @param target      发送目标
     * @param content     文本内容（可为 null，仅发附件）
     * @param attachments 附件列表（图片/链接/小程序）
     * @return API 响应
     */
    public Map<String, Object> send(ExternalGroupTarget target,
                                     String content,
                                     List<ExternalMessage> attachments) {
        return sendGroupMessage(target, content, attachments);
    }

    /**
     * 创建企业群发任务（仅附件，无文本）。
     */
    public Map<String, Object> send(ExternalGroupTarget target, ExternalMessage... attachments) {
        return sendGroupMessage(target, null, Arrays.asList(attachments));
    }

    /**
     * 创建企业群发任务（文本 + 单个附件）。
     */
    public Map<String, Object> send(ExternalGroupTarget target, String content, ExternalMessage attachment) {
        return sendGroupMessage(target, content, List.of(attachment));
    }

    private Map<String, Object> sendGroupMessage(ExternalGroupTarget target,
                                                  String content,
                                                  List<ExternalMessage> attachments) {
        Map<String, Object> payload = new LinkedHashMap<>();
        target.applyTo(payload);

        if (content != null && !content.isEmpty()) {
            payload.put("text", Map.of("content", content));
        }

        if (attachments != null && !attachments.isEmpty()) {
            List<Map<String, Object>> attachmentList = attachments.stream()
                    .map(ExternalMessage::toAttachment)
                    .toList();
            payload.put("attachments", attachmentList);
        }

        try {
            String json = objectMapper.writeValueAsString(payload);
            log.debug("Sending external group message: {}", json);

            String url = apiBase + "/externalcontact/add_msg_template?access_token=" + getAccessToken();
            RequestBody body = RequestBody.create(json, JSON_MEDIA_TYPE);
            Request request = new Request.Builder().url(url).post(body).build();

            try (Response response = httpClient.newCall(request).execute()) {
                Map<String, Object> result = parseResponse(response);
                log.info("External group message created: msgid={}, chat_type={}",
                        result.get("msgid"), target.getChatType());
                return result;
            }
        } catch (WeComExternalException e) {
            throw e;
        } catch (IOException e) {
            throw new WeComExternalException("创建群发任务失败", e);
        }
    }

    // ── 查询群发结果 ─────────────────────────────────────────────

    /**
     * 查询群发任务的发送结果。
     *
     * @param msgid  群发任务 ID（创建群发时返回）
     * @param cursor 分页游标，首次传空字符串
     * @param limit  每页条数，最大 1000
     * @return API 响应（含 send_list 发送详情列表）
     */
    public Map<String, Object> getGroupMsgResult(String msgid, String cursor, int limit) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("msgid", msgid);
        if (cursor != null && !cursor.isEmpty()) {
            payload.put("cursor", cursor);
        }
        payload.put("limit", limit);

        try {
            String json = objectMapper.writeValueAsString(payload);
            String url = apiBase + "/externalcontact/get_groupmsg_send_result?access_token=" + getAccessToken();
            RequestBody body = RequestBody.create(json, JSON_MEDIA_TYPE);
            Request request = new Request.Builder().url(url).post(body).build();

            try (Response response = httpClient.newCall(request).execute()) {
                return parseResponse(response);
            }
        } catch (WeComExternalException e) {
            throw e;
        } catch (IOException e) {
            throw new WeComExternalException("查询群发结果失败", e);
        }
    }

    // ── 内部工具 ─────────────────────────────────────────────────

    private Map<String, Object> parseResponse(Response response) throws IOException {
        if (!response.isSuccessful()) {
            throw new WeComExternalException(-1, "HTTP " + response.code() + ": " + response.message());
        }
        String responseBody = response.body() != null ? response.body().string() : "{}";
        Map<String, Object> result = objectMapper.readValue(
                responseBody, new TypeReference<>() {}
        );
        Object errcodeObj = result.get("errcode");
        int errcode = errcodeObj instanceof Number ? ((Number) errcodeObj).intValue() : 0;
        if (errcode != 0) {
            String errmsg = (String) result.getOrDefault("errmsg", "unknown error");
            throw new WeComExternalException(errcode, errmsg);
        }
        return result;
    }

    private static class CachedToken {
        private static final long ADVANCE_EXPIRE_MS = 5 * 60 * 1000L;
        final String token;
        final long expireAt;

        CachedToken(String token, int expiresInSec) {
            this.token = token;
            this.expireAt = System.currentTimeMillis() + expiresInSec * 1000L - ADVANCE_EXPIRE_MS;
        }

        boolean isExpired() {
            return System.currentTimeMillis() >= expireAt;
        }
    }
}
