package com.gjy.domain.order.service;

import com.gjy.domain.order.model.entity.PayOrderEntity;
import com.gjy.domain.order.model.entity.ProductEntity;
import com.gjy.domain.order.model.entity.ShopCartEntity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface IOrderService {
    /**
     * 查询商品列表
     */
    List<ProductEntity> queryProductList();

    PayOrderEntity createOrder(ShopCartEntity shopCartEntity);

    void changeOrderPaySuccess(String orderId, String transactionId, BigDecimal totalAmount, Date payTime);

    void deliverGoods(String orderId);

    List<String> queryReplenishmentOrder();

    List<String> queryTimeoutCloseOrderList();

    boolean changeOrderClose(String orderId);
}
