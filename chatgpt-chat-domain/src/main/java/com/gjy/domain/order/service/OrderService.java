package com.gjy.domain.order.service;

import com.gjy.domain.order.model.entity.ProductEntity;
import com.gjy.domain.order.repository.IOrderRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Service
public class OrderService extends AbstractOrderService{

    @Resource
    private IOrderRepository orderRepository;
    @Override
    public List<ProductEntity> queryProductList() {
        return orderRepository.queryProductList();
    }
}
