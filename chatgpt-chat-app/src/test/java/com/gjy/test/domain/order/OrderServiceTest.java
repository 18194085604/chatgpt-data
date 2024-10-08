package com.gjy.test.domain.order;

import com.gjy.domain.order.model.entity.PayOrderEntity;
import com.gjy.domain.order.model.entity.ShopCartEntity;
import com.gjy.domain.order.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderServiceTest {

    @Resource
    private IOrderService orderService;

    @Test
    public void test_createOrder() {
        ShopCartEntity shopCartEntity = ShopCartEntity.builder()
                .openId("gjy-test02")
                .productId(1001)
                .build();
        PayOrderEntity payOrderEntity = orderService.createOrder(shopCartEntity);
        log.info("请求参数：{} 测试结果: {}", shopCartEntity, payOrderEntity);
    }

}
