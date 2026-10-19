package com.example.wechatpay.dto;

/**
 * 创建订单请求体。
 */
public class CreateOrderRequest {

    /** 金额，单位：元，例如 66 或 66.00。仅允许固定额度白名单内的金额。 */
    private Double amount;

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
