package com.example.wechatpay.exception;

import com.example.wechatpay.dto.ApiResponse;
import com.github.binarywang.wxpay.exception.WxPayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：统一返回 {code, message} JSON。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResponse<Void>> handleBiz(BizException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(WxPayException.class)
    public ResponseEntity<ApiResponse<Void>> handleWxPay(WxPayException e) {
        log.error("微信支付调用失败: {}", e.getMessage());
        String message = (e.getErrCodeDes() != null && !e.getErrCodeDes().isBlank())
                ? e.getErrCodeDes() : e.getMessage();
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponse.error(502, "微信支付调用失败：" + message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleOther(Exception e) {
        log.error("系统异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "系统繁忙，请稍后再试"));
    }
}
