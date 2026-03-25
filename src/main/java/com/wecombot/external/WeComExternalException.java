package com.wecombot.external;

/**
 * 企业微信客户联系 API 异常。
 */
public class WeComExternalException extends RuntimeException {

    private final int errcode;
    private final String errmsg;

    public WeComExternalException(int errcode, String errmsg) {
        super("[" + errcode + "] " + errmsg);
        this.errcode = errcode;
        this.errmsg = errmsg;
    }

    public WeComExternalException(String message, Throwable cause) {
        super(message, cause);
        this.errcode = -1;
        this.errmsg = message;
    }

    public int getErrcode() { return errcode; }
    public String getErrmsg() { return errmsg; }
}
