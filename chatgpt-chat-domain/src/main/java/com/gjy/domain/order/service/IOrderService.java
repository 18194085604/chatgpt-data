package com.gjy.domain.order.service;

import com.gjy.domain.order.model.entity.ProductEntity;

import java.util.List;

public interface IOrderService {
    /**
     * 查询商品列表
     */
    List<ProductEntity> queryProductList();
}
