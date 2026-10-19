package com.example.wechatpay.config;

import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 微信支付 SDK 装配。密钥只保存在服务端配置里，绝不进入前端。
 */
@Configuration
public class WechatPayConfig {

    @Bean
    public WxPayService wxPayService(WechatPayProperties props) {
        WxPayConfig config = new WxPayConfig();
        config.setAppId(props.getAppId());
        config.setMchId(props.getMchId());
        config.setMchKey(props.getMchKey());
        config.setNotifyUrl(props.getNotifyUrl());
        WxPayServiceImpl service = new WxPayServiceImpl();
        service.setConfig(config);
        return service;
    }
}
