package com.example.wechatpay.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 微信支付配置，对应 application.yml 中 wechat.pay.* 前缀。
 */
@ConfigurationProperties(prefix = "wechat.pay")
public class WechatPayProperties {

    /** 公众号 / 小程序 / APP 的 AppID（需在商户平台与商户号关联） */
    private String appId = "";

    /** 微信支付商户号 */
    private String mchId = "";

    /** APIv2 密钥（32 位，商户平台 → 账户中心 → API安全 中设置） */
    private String mchKey = "";

    /** 支付结果回调地址，必须是公网可访问的 HTTPS 地址，例如 https://api.example.com/api/notify */
    private String notifyUrl = "";

    /** 商品描述，会展示在用户的支付确认页 */
    private String description = "收款";

    /** 允许的固定金额（单位：元），服务端强制白名单校验，需与前端页面金额一致 */
    private List<Integer> allowAmounts = new ArrayList<>(List.of(6, 18, 66, 88, 188));

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public String getMchKey() {
        return mchKey;
    }

    public void setMchKey(String mchKey) {
        this.mchKey = mchKey;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Integer> getAllowAmounts() {
        return allowAmounts;
    }

    public void setAllowAmounts(List<Integer> allowAmounts) {
        this.allowAmounts = allowAmounts;
    }
}
