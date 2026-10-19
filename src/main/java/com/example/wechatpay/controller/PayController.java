package com.example.wechatpay.controller;

import com.example.wechatpay.dto.ApiResponse;
import com.example.wechatpay.dto.CreateOrderRequest;
import com.example.wechatpay.dto.CreateOrderResponse;
import com.example.wechatpay.dto.OrderStatusResponse;
import com.example.wechatpay.exception.BizException;
import com.example.wechatpay.service.PayService;
import com.github.binarywang.wxpay.exception.WxPayException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付接口：创建订单、查询状态。
 */
@RestController
@RequestMapping("/api")
public class PayController {

    private final PayService payService;

    public PayController(PayService payService) {
        this.payService = payService;
    }

    /**
     * 创建固定金额的 Native 支付订单。
     * 请求体：{"amount": 66}
     */
    @PostMapping("/create-order")
    public ApiResponse<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest req,
                                                        HttpServletRequest request) throws WxPayException {
        Double amount = req.getAmount();
        if (amount == null || amount <= 0) {
            throw new BizException("金额不能为空");
        }
        return ApiResponse.ok(payService.createOrder(amount, resolveClientIp(request)));
    }

    /**
     * 查询订单支付状态，前端轮询使用。
     */
    @GetMapping("/order-status")
    public ApiResponse<OrderStatusResponse> orderStatus(@RequestParam String outTradeNo) throws WxPayException {
        return ApiResponse.ok(payService.queryStatus(outTradeNo));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String xReal = request.getHeader("X-Real-IP");
        if (xReal != null && !xReal.isBlank()) {
            return xReal.trim();
        }
        return request.getRemoteAddr();
    }
}
