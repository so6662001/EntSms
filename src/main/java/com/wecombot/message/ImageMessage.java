package com.wecombot.message;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * 图片消息。
 *
 * <p>图片大小限制 2MB，支持 JPG/PNG 格式。
 * 使用 {@link #fromFile(Path)} 或 {@link #fromBytes(byte[])} 创建实例。
 */
public class ImageMessage implements WeComMessage {

    private final String base64Data;
    private final String md5;

    public ImageMessage(String base64Data, String md5) {
        this.base64Data = Objects.requireNonNull(base64Data);
        this.md5 = Objects.requireNonNull(md5);
    }

    public static ImageMessage fromFile(Path path) throws IOException {
        byte[] data = Files.readAllBytes(path);
        return fromBytes(data);
    }

    public static ImageMessage fromFile(String path) throws IOException {
        return fromFile(Path.of(path));
    }

    public static ImageMessage fromBytes(byte[] data) {
        String base64 = Base64.getEncoder().encodeToString(data);
        String md5 = md5Hex(data);
        return new ImageMessage(base64, md5);
    }

    private static String md5Hex(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    @Override
    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("msgtype", "image");
        payload.put("image", Map.of("base64", base64Data, "md5", md5));
        return payload;
    }

    public String getBase64Data() { return base64Data; }
    public String getMd5() { return md5; }
}
