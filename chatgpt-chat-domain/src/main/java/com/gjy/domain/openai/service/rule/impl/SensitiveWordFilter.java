package com.gjy.domain.openai.service.rule.impl;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.gjy.domain.openai.annotation.LogicStrategy;
import com.gjy.domain.openai.model.aggregates.ChatProcessAggregate;
import com.gjy.domain.openai.model.entity.MessageEntity;
import com.gjy.domain.openai.model.entity.RuleLogicEntity;
import com.gjy.domain.openai.model.entity.UserAccountQuotaEntity;
import com.gjy.domain.openai.model.valobj.LogicCheckTypeVO;
import com.gjy.domain.openai.service.rule.ILogicFilter;
import com.gjy.domain.openai.service.rule.factory.DefaultLogicFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.SENSITIVE_WORD)
public class SensitiveWordFilter implements ILogicFilter<UserAccountQuotaEntity> {

    @Value("${app.config.white-list}")
    private String whiteListStr;

    @Resource
    private SensitiveWordBs words;

    @Override
    public RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess, UserAccountQuotaEntity data) throws Exception {
        // 白名单直接放过
        if (chatProcess.isWhiteList(whiteListStr)){
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .data(chatProcess)
                    .type(LogicCheckTypeVO.SUCCESS)
                    .build();
        }
        ChatProcessAggregate chatProcessAggregate = new ChatProcessAggregate();
        chatProcessAggregate.setModel(chatProcess.getModel());
        chatProcessAggregate.setOpenId(chatProcess.getOpenId());
        List<MessageEntity> newMessages = chatProcess.getMessages().stream().map(message -> {
            String content = message.getContent();
            String replace = words.replace(content);
            return MessageEntity.builder().role(message.getRole())
                    .name(message.getName())
                    .content(replace)
                    .build();
        }).collect(Collectors.toList());
        chatProcessAggregate.setMessages(newMessages);
        return RuleLogicEntity.<ChatProcessAggregate>builder()
                .type(LogicCheckTypeVO.SUCCESS).data(chatProcessAggregate).build();
    }
}
