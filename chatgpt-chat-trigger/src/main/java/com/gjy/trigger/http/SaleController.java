package com.gjy.trigger.http;

import com.alibaba.fastjson.JSON;
import com.alipay.api.internal.util.AlipaySignature;
import com.gjy.domain.auth.service.AuthService;
import com.gjy.domain.order.model.entity.PayOrderEntity;
import com.gjy.domain.order.model.entity.ProductEntity;
import com.gjy.domain.order.model.entity.ShopCartEntity;
import com.gjy.domain.order.service.IOrderService;
import com.gjy.trigger.http.dto.SaleProductDTO;
import com.gjy.types.common.Constants;
import com.gjy.types.model.Response;
import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController()
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/sale/")
public class SaleController {

    @Resource
    private AuthService authService;

    @Resource
    private IOrderService orderService;

    @Resource
    private EventBus eventBus;

    @Value("${alipay.alipay_public_key}")
    private String alipayPublicKey;

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

    /**
     * 支付回调
     * 开发地址：http:/localhost:8091/api/v1/sale/pay_notify
     * 测试地址：http://apix.natapp1.cc/api/v1/sale/pay_notify
     * 线上地址：https://你的域名/api/v1/sale/pay_notify
     */
    @PostMapping("pay_notify")
    public String payNotify(HttpServletRequest request){
        try {
            log.info("支付回调，消息接收 {}", request.getParameter("trade_status"));
            if (request.getParameter("trade_status").equals("TRADE_SUCCESS")) {
                Map<String, String> params = new HashMap<>();
                Map<String, String[]> requestParams = request.getParameterMap();
                for (String name : requestParams.keySet()) {
                    params.put(name, request.getParameter(name));
                }

                String tradeNo = params.get("out_trade_no");
                String gmtPayment = params.get("gmt_payment");
                String alipayTradeNo = params.get("trade_no");

                String sign = params.get("sign");
                String content = AlipaySignature.getSignCheckContentV1(params);
                boolean checkSignature = AlipaySignature.rsa256CheckContent(content, sign, alipayPublicKey, "UTF-8"); // 验证签名
                // 支付宝验签
                if (checkSignature) {
                    // 验签通过
                    log.info("支付回调，交易名称: {}", params.get("subject"));
                    log.info("支付回调，交易状态: {}", params.get("trade_status"));
                    log.info("支付回调，支付宝交易凭证号: {}", params.get("trade_no"));
                    log.info("支付回调，商户订单号: {}", params.get("out_trade_no"));
                    log.info("支付回调，交易金额: {}", params.get("total_amount"));
                    log.info("支付回调，买家在支付宝唯一id: {}", params.get("buyer_id"));
                    log.info("支付回调，买家付款时间: {}", params.get("gmt_payment"));
                    log.info("支付回调，买家付款金额: {}", params.get("buyer_pay_amount"));
                    log.info("支付回调，支付回调，更新订单 {}", tradeNo);
                    // 更新订单未已支付
                    orderService.changeOrderPaySuccess(tradeNo,alipayTradeNo,new BigDecimal(gmtPayment),new Date(params.get("gmt_payment")));
                    // 推送消息【自己的业务场景中可以使用MQ消息】
                    eventBus.post(tradeNo);
                }
            }
            return "success";
        } catch (Exception e) {
            log.error("支付回调，处理失败", e);
            return "false";
        }
    }



}
