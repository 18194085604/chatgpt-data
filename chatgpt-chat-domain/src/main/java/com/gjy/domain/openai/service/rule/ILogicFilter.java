package com.gjy.domain.openai.service.rule;

import com.gjy.domain.openai.model.aggregates.ChatProcessAggregate;
import com.gjy.domain.openai.model.entity.RuleLogicEntity;

public interface ILogicFilter {
    RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess) throws Exception;
}
