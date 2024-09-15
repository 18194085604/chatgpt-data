package com.gjy.domain.order.repository;

import com.gjy.domain.order.model.aggregates.CreateOrderAggregate;
import com.gjy.domain.order.model.entity.PayOrderEntity;
import com.gjy.domain.order.model.entity.ProductEntity;
import com.gjy.domain.order.model.entity.ShopCartEntity;
import com.gjy.domain.order.model.entity.UnpaidOrderEntity;

import java.util.List;

public interface IOrderRepository {

    List<ProductEntity> queryProductList();

    UnpaidOrderEntity queryUnpaidOrder(ShopCartEntity shopCartEntity);

    ProductEntity queryProduct(Integer productId);

    void saveOrder(CreateOrderAggregate aggregate);

    void updateOrderPayInfo(PayOrderEntity payOrderEntity);
}
