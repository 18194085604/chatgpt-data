package com.gjy.domain.order.repository;

import com.gjy.domain.order.model.entity.ProductEntity;

import java.util.List;

public interface IOrderRepository {

    List<ProductEntity> queryProductList();
}
