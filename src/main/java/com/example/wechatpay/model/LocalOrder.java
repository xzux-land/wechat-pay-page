package com.example.wechatpay.model;

/**
 * 本地订单记录。演示使用内存存储，生产环境请替换为数据库 / Redis。
 */
public class LocalOrder {

    private final String outTradeNo;

    /** 订单金额，单位：分 */
    private final int amountFen;

    private final long createTime;

    private volatile boolean paid;

    public LocalOrder(String outTradeNo, int amountFen) {
        this.outTradeNo = outTradeNo;
        this.amountFen = amountFen;
        this.createTime = System.currentTimeMillis();
        this.paid = false;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public int getAmountFen() {
        return amountFen;
    }

    public long getCreateTime() {
        return createTime;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }
}
