package com.wecombot.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wecombot.message.*;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 企业微信群机器人客户端。
 *
 * <p>通过 Webhook 向企业微信外部群发送消息。
 *
 * <pre>{@code
 * WeComBotClient client = new WeComBotClient("your-webhook-key");
 * client.sendText("Hello，这是一条测试消息！");
 * }</pre>
 */
public class WeComBotClient {

    private static final Logger log = LoggerFactory.getLogger(WeComBotClient.class);
    private static final String DEFAULT_BASE_URL = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send";
    private static final String UPLOAD_BASE_URL = "https://qyapi.weixin.qq.com/cgi-bin/webhook/upload_media";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private final String key;
    private final String baseUrl;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * 使用默认配置创建客户端。
     *
     * @param key Webhook URL 中的 key 参数
     */
    public WeComBotClient(String key) {
        this(key, DEFAULT_BASE_URL, 10);
    }

    /**
     * 创建客户端。
     *
     * @param key        Webhook URL 中的 key 参数
     * @param baseUrl    Webhook 基础 URL
     * @param timeoutSec 请求超时秒数
     */
    public WeComBotClient(String key, String baseUrl, int timeoutSec) {
        this.key = Objects.requireNonNull(key, "key 不能为空");
        if (key.isBlank()) {
            throw new IllegalArgumentException("key 不能为空");
        }
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl 不能为空");
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
    WeComBotClient(String key, String baseUrl, OkHttpClient httpClient) {
        this.key = key;
        this.baseUrl = baseUrl;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    public String getWebhookUrl() {
        return baseUrl + "?key=" + key;
    }

    /**
     * 发送消息。
     *
     * @param message 消息对象
     * @return API 响应 Map
     * @throws WeComBotException API 错误或网络异常时抛出
     */
    public Map<String, Object> send(WeComMessage message) {
        Map<String, Object> payload = message.toPayload();
        try {
            String json = objectMapper.writeValueAsString(payload);
            log.debug("Sending payload: {}", json);

            RequestBody body = RequestBody.create(json, JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                    .url(getWebhookUrl())
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                return handleResponse(response);
            }
        } catch (WeComBotException e) {
            throw e;
        } catch (IOException e) {
            throw new WeComBotException("发送消息失败", e);
        }
    }

    // ── 快捷方法 ──────────────────────────────────────────────

    public Map<String, Object> sendText(String content) {
        return send(TextMessage.of(content));
    }

    public Map<String, Object> sendText(String content, List<String> mentionedList) {
        return send(TextMessage.builder(content).mentionedList(mentionedList).build());
    }

    public Map<String, Object> sendMarkdown(String content) {
        return send(MarkdownMessage.of(content));
    }

    public Map<String, Object> sendImage(Path path) throws IOException {
        return send(ImageMessage.fromFile(path));
    }

    public Map<String, Object> sendImage(byte[] imageData) {
        return send(ImageMessage.fromBytes(imageData));
    }

    /**
     * 上传文件并返回 media_id。
     *
     * @param path 本地文件路径
     * @return media_id
     */
    public String uploadFile(Path path) {
        try {
            String fileName = path.getFileName().toString();
            byte[] fileData = Files.readAllBytes(path);
            RequestBody fileBody = RequestBody.create(fileData, MediaType.get("application/octet-stream"));

            MultipartBody multipartBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("media", fileName, fileBody)
                    .build();

            String uploadUrl = UPLOAD_BASE_URL + "?key=" + key + "&type=file";
            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .post(multipartBody)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                Map<String, Object> result = handleResponse(response);
                String mediaId = (String) result.get("media_id");
                log.info("File uploaded: {} -> media_id={}", fileName, mediaId);
                return mediaId;
            }
        } catch (WeComBotException e) {
            throw e;
        } catch (IOException e) {
            throw new WeComBotException("上传文件失败", e);
        }
    }

    /**
     * 上传并发送文件。
     */
    public Map<String, Object> sendFile(Path path) {
        String mediaId = uploadFile(path);
        return send(FileMessage.of(mediaId));
    }

    private Map<String, Object> handleResponse(Response response) throws IOException {
        if (!response.isSuccessful()) {
            throw new WeComBotException(-1, "HTTP " + response.code() + ": " + response.message());
        }

        String responseBody = response.body() != null ? response.body().string() : "{}";
        Map<String, Object> result = objectMapper.readValue(
                responseBody,
                new TypeReference<Map<String, Object>>() {}
        );

        Object errcodeObj = result.get("errcode");
        int errcode = errcodeObj instanceof Number ? ((Number) errcodeObj).intValue() : 0;
        if (errcode != 0) {
            String errmsg = (String) result.getOrDefault("errmsg", "unknown error");
            throw new WeComBotException(errcode, errmsg);
        }

        log.info("Message sent successfully: msgtype={}", result.getOrDefault("msgtype",
                ((Map<?, ?>) result).keySet()));
        return result;
    }
}
