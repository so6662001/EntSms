package com.wecombot.app;

/**
 * 企业微信应用消息 API 异常。
 */
public class WeComAppException extends RuntimeException {

    private final int errcode;
    private final String errmsg;

    public WeComAppException(int errcode, String errmsg) {
        super("[" + errcode + "] " + errmsg);
        this.errcode = errcode;
        this.errmsg = errmsg;
    }

    public WeComAppException(String message, Throwable cause) {
        super(message, cause);
        this.errcode = -1;
        this.errmsg = message;
    }

    public int getErrcode() { return errcode; }
    public String getErrmsg() { return errmsg; }
}
