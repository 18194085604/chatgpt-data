package com.gjy.domain.openai.service.rule.impl;

import com.gjy.domain.openai.annotation.LogicStrategy;
import com.gjy.domain.openai.model.aggregates.ChatProcessAggregate;
import com.gjy.domain.openai.model.entity.RuleLogicEntity;
import com.gjy.domain.openai.model.entity.UserAccountQuotaEntity;
import com.gjy.domain.openai.model.valobj.LogicCheckTypeVO;
import com.gjy.domain.openai.model.valobj.UserAccountStatusVO;
import com.gjy.domain.openai.service.rule.ILogicFilter;
import com.gjy.domain.openai.service.rule.factory.DefaultLogicFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * 账户校验
 */
@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.ACCOUNT_STATUS)
public class AccountStatusFilter implements ILogicFilter<UserAccountQuotaEntity> {

    @Override
    public RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess, UserAccountQuotaEntity data) throws Exception {
        if (UserAccountStatusVO.AVAILABLE.equals(data.getUserAccountStatusVO())){
            return RuleLogicEntity.<ChatProcessAggregate>builder().type(LogicCheckTypeVO.SUCCESS).data(chatProcess).build();
        }
        return RuleLogicEntity.<ChatProcessAggregate>builder()
                .info("您的账户已冻结，暂时不可使用。如果有疑问，可以联系客户解冻账户。")
                .type(LogicCheckTypeVO.REFUSE).data(chatProcess).build();
    }
}
