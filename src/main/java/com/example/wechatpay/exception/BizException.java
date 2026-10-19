package com.example.wechatpay.exception;

/**
 * 业务异常（参数错误、金额不合法等），由全局异常处理器转为 4xx 响应。
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        this(400, message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
