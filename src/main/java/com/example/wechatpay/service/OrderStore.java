package com.example.wechatpay.service;

import com.example.wechatpay.model.LocalOrder;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 订单内存存储，仅用于演示与轻量场景。
 * 生产环境请替换为 MySQL / Redis 等持久化存储，并定期清理过期订单。
 */
@Component
public class OrderStore {

    private final ConcurrentHashMap<String, LocalOrder> orders = new ConcurrentHashMap<>();

    public void put(LocalOrder order) {
        orders.put(order.getOutTradeNo(), order);
    }

    public LocalOrder get(String outTradeNo) {
        return orders.get(outTradeNo);
    }

    public void markPaid(String outTradeNo) {
        LocalOrder order = orders.get(outTradeNo);
        if (order != null) {
            order.setPaid(true);
        }
    }
}
