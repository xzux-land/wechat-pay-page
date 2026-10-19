package com.example.wechatpay.service;

import com.example.wechatpay.config.WechatPayProperties;
import com.example.wechatpay.dto.CreateOrderResponse;
import com.example.wechatpay.dto.OrderStatusResponse;
import com.example.wechatpay.exception.BizException;
import com.example.wechatpay.model.LocalOrder;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryResult;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 微信支付核心服务：Native 扫码下单、订单查询、支付回调处理。
 */
@Service
public class PayService {

    private static final Logger log = LoggerFactory.getLogger(PayService.class);

    private static final int QR_SIZE = 320;

    /** 二维码有效期（秒），与微信 Native 订单默认有效期一致 */
    private static final long EXPIRE_SECONDS = 7200;

    private final WxPayService wxPayService;
    private final WechatPayProperties props;
    private final OrderStore orderStore;

    /** 允许的金额白名单（单位：分），由配置的 allow-amounts（元）换算而来 */
    private final Set<Integer> allowedAmountFen;

    public PayService(WxPayService wxPayService, WechatPayProperties props, OrderStore orderStore) {
        this.wxPayService = wxPayService;
        this.props = props;
        this.orderStore = orderStore;
        this.allowedAmountFen = new HashSet<>();
        for (Integer yuan : props.getAllowAmounts()) {
            if (yuan != null && yuan > 0) {
                allowedAmountFen.add(yuan * 100);
            }
        }
    }

    /**
     * 创建 Native 支付订单，返回付款二维码。
     *
     * @param amountYuan 金额（元）
     * @param clientIp   用户终端 IP（微信要求必填）
     */
    public CreateOrderResponse createOrder(double amountYuan, String clientIp) throws WxPayException {
        int amountFen = (int) Math.round(amountYuan * 100);
        if (amountFen <= 0 || !allowedAmountFen.contains(amountFen)) {
            throw new BizException("金额不合法，仅支持固定金额：" + props.getAllowAmounts() + " 元");
        }

        String outTradeNo = buildOutTradeNo();

        WxPayUnifiedOrderRequest req = new WxPayUnifiedOrderRequest();
        req.setBody(props.getDescription());
        req.setOutTradeNo(outTradeNo);
        req.setTotalFee(amountFen);
        req.setSpbillCreateIp(clientIp);
        req.setNotifyUrl(props.getNotifyUrl());
        req.setTradeType("NATIVE");
        req.setProductId(outTradeNo);

        WxPayUnifiedOrderResult result = wxPayService.unifiedOrder(req);
        String codeUrl = result.getCodeURL();

        orderStore.put(new LocalOrder(outTradeNo, amountFen));

        CreateOrderResponse resp = new CreateOrderResponse();
        resp.setOutTradeNo(outTradeNo);
        resp.setCodeUrl(codeUrl);
        resp.setQrCodeDataUrl(generateQrDataUrl(codeUrl));
        resp.setExpireSeconds(EXPIRE_SECONDS);
        log.info("已创建订单 outTradeNo={} 金额={}分", outTradeNo, amountFen);
        return resp;
    }

    /**
     * 查询订单状态（以微信侧返回为准）。
     */
    public OrderStatusResponse queryStatus(String outTradeNo) throws WxPayException {
        WxPayOrderQueryResult query = wxPayService.queryOrder(null, outTradeNo);
        String tradeState = query.getTradeState();
        boolean paid = "SUCCESS".equals(tradeState);
        if (paid) {
            orderStore.markPaid(outTradeNo);
        }
        return new OrderStatusResponse(tradeState, paid);
    }

    /**
     * 处理微信支付结果回调（服务端到服务端）。SDK 会校验签名，金额需与本地订单核对。
     */
    public String handleNotify(String xmlData) throws WxPayException {
        WxPayOrderNotifyResult result = wxPayService.parseOrderNotifyResult(xmlData);
        String outTradeNo = result.getOutTradeNo();

        LocalOrder order = orderStore.get(outTradeNo);
        if (order == null) {
            // 本地无此订单（如服务重启丢失），仍应答成功，避免微信反复推送
            log.warn("收到未知订单的回调，应答成功: {}", outTradeNo);
            return successXml();
        }

        Integer totalFee = result.getTotalFee();
        int fee = (totalFee == null) ? 0 : totalFee;
        if (fee != order.getAmountFen()) {
            log.error("回调金额与本地订单不一致 outTradeNo={} 本地={}分 回调={}分",
                    outTradeNo, order.getAmountFen(), fee);
            throw new BizException(500, "回调金额不一致");
        }

        orderStore.markPaid(outTradeNo);
        log.info("订单支付成功 outTradeNo={} 金额={}分", outTradeNo, fee);
        return successXml();
    }

    private String successXml() {
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code>"
                + "<return_msg><![CDATA[OK]]></return_msg></xml>";
    }

    private String buildOutTradeNo() {
        return "WX" + System.currentTimeMillis()
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

    private String generateQrDataUrl(String text) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            log.error("生成二维码失败", e);
            throw new BizException(500, "二维码生成失败");
        }
    }
}
