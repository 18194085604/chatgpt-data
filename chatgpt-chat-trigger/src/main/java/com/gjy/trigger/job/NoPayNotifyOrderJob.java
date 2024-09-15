package com.gjy.trigger.job;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.gjy.domain.order.model.valobj.PayStatusVO;
import com.gjy.domain.order.service.IOrderService;
import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 检测未接收到或未正确处理的支付回调通知
 */
@Slf4j
@Component
public class NoPayNotifyOrderJob {

    @Resource
    private AlipayClient alipayClient;

    @Resource
    private IOrderService orderService;

    @Resource
    private EventBus eventBus;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void exec() {
        try {
            List<String> orderIds = orderService.queryNoPayNotifyOrder();
            if (orderIds.isEmpty()) {
                log.info("定时任务，订单支付状态更新，暂无未更新订单 orderId is null");
                return;
            }
            for (String orderId : orderIds) {
                AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
                AlipayTradeQueryModel model = new AlipayTradeQueryModel();
                model.setOutTradeNo(orderId);
                request.setBizModel(model);
                AlipayTradeQueryResponse response = alipayClient.execute(request);
                String tradeStatus = response.getTradeStatus();
                if (!"WAIT_BUYER_PAY".equals(tradeStatus)) {
                    log.info("定时任务，订单支付状态更新，当前订单未支付 orderId is {}", orderId);
                    continue;
                }
                // 支付单号
                String transactionId = response.getTradeNo();
                BigDecimal totalAmount = new BigDecimal(response.getBuyerPayAmount());
                Date successTime = response.getSendPayDate();
                // 更新订单
                boolean isSuccess = orderService.changeOrderPaySuccess(orderId, transactionId, totalAmount, successTime);
                if (isSuccess) {
                    // 发布消息
                    eventBus.post(orderId);
                }
            }
        } catch (AlipayApiException e) {
            log.error("定时任务，订单支付状态更新失败", e);
        }
    }
}
