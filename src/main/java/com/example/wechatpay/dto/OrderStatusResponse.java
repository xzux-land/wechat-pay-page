package com.example.wechatpay.dto;

/**
 * 订单状态查询响应。
 */
public class OrderStatusResponse {

    /** 微信支付订单状态：SUCCESS / NOTPAY / CLOSED / PAYERROR / USERPAYING / REVOKED */
    private String tradeState;

    /** 是否已支付成功 */
    private boolean paid;

    public OrderStatusResponse(String tradeState, boolean paid) {
        this.tradeState = tradeState;
        this.paid = paid;
    }

    public String getTradeState() {
        return tradeState;
    }

    public void setTradeState(String tradeState) {
        this.tradeState = tradeState;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }
}
