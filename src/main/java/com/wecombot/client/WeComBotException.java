package com.wecombot.client;

/**
 * 企业微信机器人 API 异常。
 */
public class WeComBotException extends RuntimeException {

    private final int errcode;
    private final String errmsg;

    public WeComBotException(int errcode, String errmsg) {
        super("[" + errcode + "] " + errmsg);
        this.errcode = errcode;
        this.errmsg = errmsg;
    }

    public WeComBotException(String message, Throwable cause) {
        super(message, cause);
        this.errcode = -1;
        this.errmsg = message;
    }

    public int getErrcode() { return errcode; }
    public String getErrmsg() { return errmsg; }
}
