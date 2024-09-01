package com.gjy.domain.openai.service.rule.impl;

import com.gjy.domain.openai.annotation.LogicStrategy;
import com.gjy.domain.openai.model.aggregates.ChatProcessAggregate;
import com.gjy.domain.openai.model.entity.RuleLogicEntity;
import com.gjy.domain.openai.model.entity.UserAccountQuotaEntity;
import com.gjy.domain.openai.model.valobj.LogicCheckTypeVO;
import com.gjy.domain.openai.service.rule.ILogicFilter;
import com.gjy.domain.openai.service.rule.factory.DefaultLogicFactory;
import com.google.common.cache.Cache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.ACCESS_LIMIT)
public class AccessLimitFilter implements ILogicFilter<UserAccountQuotaEntity> {

    @Value("${app.config.white-list}")
    private String whiteList;

    @Value("${app.config.limit-count:10}")
    private Integer limitCount;

    @Resource
    private Cache<String, Integer> visitCache;

    @Override
    public RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess, UserAccountQuotaEntity data) throws Exception {
        // 1. 白名单用户直接放行
        if (chatProcess.isWhiteList(whiteList)) {
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .data(chatProcess)
                    .type(LogicCheckTypeVO.SUCCESS).build();
        }
        // 个人账户不为空，不做拦截处理
        if (data != null) {
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .data(chatProcess)
                    .type(LogicCheckTypeVO.SUCCESS).build();
        }
        String openid = chatProcess.getOpenId();
        // 2. 访问次数判断
        Integer visitCount = visitCache.get(openid, () -> 0);
        if (visitCount < limitCount) {
            visitCache.put(openid, visitCount + 1);
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .data(chatProcess)
                    .type(LogicCheckTypeVO.SUCCESS).build();
        }

        return RuleLogicEntity.<ChatProcessAggregate>builder()
                .info("您今日的免费" + limitCount + "次，已耗尽！")
                .type(LogicCheckTypeVO.REFUSE).data(chatProcess).build();

    }
}
