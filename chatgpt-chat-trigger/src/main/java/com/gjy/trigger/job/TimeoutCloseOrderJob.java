package com.gjy.trigger.job;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.gjy.domain.order.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 超时关单任务
 */
@Slf4j
@Component()
public class TimeoutCloseOrderJob {

    @Resource
    private AlipayClient alipayClient;

    @Resource
    private IOrderService orderService;


    @Scheduled(cron = "0 0/10 * * * ?")
    public void exec() {
        try {
            List<String> orderIds = orderService.queryTimeoutCloseOrderList();
            if (orderIds.isEmpty()) {
                log.info("定时任务，超时30分钟订单关闭，暂无超时未支付订单 orderIds is null");
                return;
            }
            for (String orderId : orderIds) {
                boolean status = orderService.changeOrderClose(orderId);
                //支付宝关单；暂时不需要主动关闭
                AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
                JSONObject bizContent = new JSONObject();
                bizContent.put("out_trade_no", orderId);
                request.setBizContent(bizContent.toString());
                AlipayTradeCloseResponse response = alipayClient.execute(request);
                if(response.isSuccess()){
                    log.info("定时任务，超时30分钟订单关闭 orderId: {} status：{}", orderId, status);
                } else {
                    log.info("定时任务关闭失败，超时30分钟订单关闭 orderId: {} status：{}", orderId, status);
                }
            }
        } catch (Exception e) {
            log.error("定时任务，超时15分钟订单关闭失败", e);
        }
    }
}
