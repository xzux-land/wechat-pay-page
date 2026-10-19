package com.example.wechatpay.controller;

import com.example.wechatpay.service.PayService;
import com.github.binarywang.wxpay.exception.WxPayException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信支付结果回调（服务端到服务端）。
 * 回调地址需在 application.yml 的 wechat.pay.notify-url 中配置为公网 HTTPS 地址。
 */
@RestController
public class NotifyController {

    private final PayService payService;

    public NotifyController(PayService payService) {
        this.payService = payService;
    }

    @PostMapping(value = "/api/notify", consumes = MediaType.TEXT_XML_VALUE, produces = MediaType.TEXT_XML_VALUE)
    public String notify(@RequestBody String xmlData) throws WxPayException {
        return payService.handleNotify(xmlData);
    }
}
