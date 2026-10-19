package com.example.wechatpay.dto;

/**
 * 创建订单成功后的响应。
 */
public class CreateOrderResponse {

    /** 商户订单号 */
    private String outTradeNo;

    /** 微信返回的支付链接（Native code_url） */
    private String codeUrl;

    /** 付款二维码图片（data URL，前端可直接 <img> 展示） */
    private String qrCodeDataUrl;

    /** 二维码有效期（秒） */
    private long expireSeconds;

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getCodeUrl() {
        return codeUrl;
    }

    public void setCodeUrl(String codeUrl) {
        this.codeUrl = codeUrl;
    }

    public String getQrCodeDataUrl() {
        return qrCodeDataUrl;
    }

    public void setQrCodeDataUrl(String qrCodeDataUrl) {
        this.qrCodeDataUrl = qrCodeDataUrl;
    }

    public long getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(long expireSeconds) {
        this.expireSeconds = expireSeconds;
    }
}
