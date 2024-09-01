package com.gjy.infrastructure.repository;

import com.gjy.domain.order.model.entity.ProductEntity;
import com.gjy.domain.order.repository.IOrderRepository;
import com.gjy.infrastructure.dao.IOpenAIProductDao;
import com.gjy.infrastructure.po.OpenAIProductPO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class OrderRepository implements IOrderRepository {
    @Resource
    private IOpenAIProductDao openAIProductDao;

    @Override
    public List<ProductEntity> queryProductList() {
        List<OpenAIProductPO> openAIProductPOList =  openAIProductDao.queryProductList();
        List<ProductEntity> productEntityList = new ArrayList<>(openAIProductPOList.size());
        for (OpenAIProductPO openAIProductPO : openAIProductPOList) {
            ProductEntity productEntity = new ProductEntity();
            productEntity.setProductId(openAIProductPO.getProductId());
            productEntity.setProductName(openAIProductPO.getProductName());
            productEntity.setProductDesc(openAIProductPO.getProductDesc());
            productEntity.setQuota(openAIProductPO.getQuota());
            productEntity.setPrice(openAIProductPO.getPrice());
            productEntityList.add(productEntity);
        }
        return productEntityList;
    }
}
