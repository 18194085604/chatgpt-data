package com.gjy.trigger.http;

import com.alibaba.fastjson.JSON;
import com.gjy.domain.auth.service.AuthService;
import com.gjy.domain.order.model.entity.PayOrderEntity;
import com.gjy.domain.order.model.entity.ProductEntity;
import com.gjy.domain.order.model.entity.ShopCartEntity;
import com.gjy.domain.order.service.IOrderService;
import com.gjy.trigger.http.dto.SaleProductDTO;
import com.gjy.types.common.Constants;
import com.gjy.types.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController()
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/sale/")
public class SaleController {

    @Resource
    private AuthService authService;

    @Resource
    private IOrderService orderService;

    @RequestMapping(value = "query_product_list", method = RequestMethod.GET)
    public Response<List<SaleProductDTO>> queryProductList(@RequestHeader("Authorization") String token) {
        // 1. Token 校验
        boolean success = authService.checkToken(token);
        if (!success){
            return Response.<List<SaleProductDTO>>builder()
                    .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                    .info(Constants.ResponseCode.TOKEN_ERROR.getInfo()).build();
        }
        // 2. 查询商品列表
        List<ProductEntity> productEntityList = orderService.queryProductList();
        log.info("商品查询 {}", JSON.toJSONString(productEntityList));
        List<SaleProductDTO> mallProductDTOS = new ArrayList<>();
        for (ProductEntity productEntity : productEntityList) {
            SaleProductDTO mallProductDTO = SaleProductDTO.builder()
                    .productId(productEntity.getProductId())
                    .productName(productEntity.getProductName())
                    .productDesc(productEntity.getProductDesc())
                    .price(productEntity.getPrice())
                    .quota(productEntity.getQuota())
                    .build();
            mallProductDTOS.add(mallProductDTO);
        }

        // 3. 返回结果
        return Response.<List<SaleProductDTO>>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info(Constants.ResponseCode.SUCCESS.getInfo())
                .data(mallProductDTOS)
                .build();
    }


    /**
     * 用户商品下单
     * 开始地址：http://localhost:8091/api/v1/sale/create_pay_order?productId=
     * 测试地址：http://apix.natapp1.cc/api/v1/sale/create_pay_order
     * <p>
     * curl -X POST \
     * -H "Authorization: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJveGZBOXc4LTI..." \
     * -H "Content-Type: application/x-www-form-urlencoded" \
     * -d "productId=1001" \
     * http://localhost:8091/api/v1/sale/create_pay_order
     */
    @RequestMapping(value = "create_pay_order", method = RequestMethod.POST)
    public Response<String> createParOrder(@RequestHeader("Authorization") String token, @RequestParam Integer productId) {
        try {
            // 1. Token 校验
            boolean success = authService.checkToken(token);
            if (!success) {
                return Response.<String>builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }

            // 解析token
            String openid = authService.openid(token);
            assert null != openid;
            log.info("用户商品下单，根据商品ID创建支付单开始 openid:{} productId:{}", openid, productId);


            ShopCartEntity shopCartEntity = ShopCartEntity.builder()
                            .openId(openid).productId(productId).build();

            PayOrderEntity payOrder = orderService.createOrder(shopCartEntity);
            log.info("用户商品下单，根据商品ID创建支付单完成 openid: {} productId: {} orderPay: {}", openid, productId, payOrder.toString());


            return Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(payOrder.getPayUrl())
                    .build();
        } catch (Exception e) {
            log.error("用户商品下单，根据商品ID创建支付单失败", e);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }



}
