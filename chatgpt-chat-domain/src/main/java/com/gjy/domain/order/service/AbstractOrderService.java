package com.gjy.domain.order.service;

import com.alipay.api.AlipayApiException;
import com.gjy.domain.order.model.entity.*;
import com.gjy.domain.order.model.valobj.PayStatusVO;
import com.gjy.domain.order.repository.IOrderRepository;
import com.gjy.types.common.Constants;
import com.gjy.types.exception.ChatGPTException;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
@Slf4j
public abstract class AbstractOrderService implements IOrderService {

    @Resource
    private IOrderRepository orderRepository;


    @Override
    public PayOrderEntity createOrder(ShopCartEntity shopCartEntity) {
        try {
            // 0. 基础信息
            String openid = shopCartEntity.getOpenId();
            Integer productId = shopCartEntity.getProductId();
            // 1.查询未支付的订单
            UnpaidOrderEntity unpaidOrderEntity = orderRepository.queryUnpaidOrder(shopCartEntity);
            if (unpaidOrderEntity != null && PayStatusVO.WAIT.equals(unpaidOrderEntity.getPayStatus()) && unpaidOrderEntity.getPayUrl() != null){
                log.info("创建订单-存在，已生成支付宝支付，返回 openid: {} orderId: {} payUrl: {}", openid, unpaidOrderEntity.getOrderId(), unpaidOrderEntity.getPayUrl());
                return PayOrderEntity.builder()
                        .openId(openid)
                        .orderId(unpaidOrderEntity.getOrderId())
                        .payUrl(unpaidOrderEntity.getPayUrl())
                        .payStatusVO(unpaidOrderEntity.getPayStatus())
                        .build();
            }else if (null != unpaidOrderEntity && null == unpaidOrderEntity.getPayUrl()) {
                log.info("创建订单-存在，未生成支付宝支付，返回 openid: {} orderId: {}", openid, unpaidOrderEntity.getOrderId());
                PayOrderEntity payOrderEntity = this.doPrepayOrder(openid, unpaidOrderEntity.getOrderId(), unpaidOrderEntity.getProductName(), unpaidOrderEntity.getTotalAmount());
                log.info("创建订单-完成，生成支付单。openid: {} orderId: {} payUrl: {}", openid, payOrderEntity.getOrderId(), payOrderEntity.getPayUrl());
                return payOrderEntity;
            }

            // 2.商品查询
            ProductEntity productEntity = orderRepository.queryProduct(productId);
            if (!productEntity.isAvailable()) {
                throw new ChatGPTException(Constants.ResponseCode.ORDER_PRODUCT_ERR.getCode(), Constants.ResponseCode.ORDER_PRODUCT_ERR.getInfo());
            }

            // 3.保存订单
            OrderEntity orderEntity = this.doSaveOrder(openid,productEntity);


            // 4.支付
            PayOrderEntity payOrderEntity = this.doPrepayOrder(openid, orderEntity.getOrderId(), productEntity.getProductName(), orderEntity.getTotalAmount());
            log.info("创建订单-完成，生成支付单。openid: {} orderId: {} payUrl: {}", openid, orderEntity.getOrderId(), payOrderEntity.getPayUrl());

            return payOrderEntity;
        } catch (ChatGPTException | AlipayApiException e) {
            log.error("创建订单，已生成支付宝支付，返回 openid: {} productId: {}", shopCartEntity.getOpenId(), shopCartEntity.getProductId());
            throw new ChatGPTException(Constants.ResponseCode.UN_ERROR.getCode(), Constants.ResponseCode.UN_ERROR.getInfo());
        }
    }

    protected abstract PayOrderEntity doPrepayOrder(String openid, String orderId, String productName, BigDecimal totalAmount) throws AlipayApiException;

    protected abstract OrderEntity doSaveOrder(String openid, ProductEntity productEntity);
}
