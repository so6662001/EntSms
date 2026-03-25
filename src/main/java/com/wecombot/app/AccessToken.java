package com.wecombot.app;

/**
 * access_token 缓存对象。
 *
 * <p>企业微信 access_token 有效期 7200 秒（2 小时），
 * 这里提前 5 分钟过期以保证安全。
 */
class AccessToken {

    private static final long ADVANCE_EXPIRE_MS = 5 * 60 * 1000L;

    private final String token;
    private final long expireAt;

    AccessToken(String token, int expiresInSec) {
        this.token = token;
        this.expireAt = System.currentTimeMillis() + expiresInSec * 1000L - ADVANCE_EXPIRE_MS;
    }

    String getToken() {
        return token;
    }

    boolean isExpired() {
        return System.currentTimeMillis() >= expireAt;
    }
}
