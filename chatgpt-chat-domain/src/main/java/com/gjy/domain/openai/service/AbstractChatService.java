package com.gjy.domain.openai.service;

import cn.bugstack.chatglm.session.OpenAiSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gjy.domain.openai.model.aggregates.ChatProcessAggregate;
import com.gjy.domain.openai.model.entity.RuleLogicEntity;
import com.gjy.domain.openai.model.entity.UserAccountQuotaEntity;
import com.gjy.domain.openai.model.valobj.LogicCheckTypeVO;
import com.gjy.domain.openai.repository.IOpenAiRepository;
import com.gjy.domain.openai.service.channel.OpenAiGroupService;
import com.gjy.domain.openai.service.channel.impl.ChatGLMService;
import com.gjy.domain.openai.service.channel.impl.ChatGPTService;
import com.gjy.domain.openai.service.rule.factory.DefaultLogicFactory;
import com.gjy.types.common.Constants;
import com.gjy.types.enums.OpenAiChannel;
import com.gjy.types.exception.ChatGPTException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class AbstractChatService implements IChatService{

    private final Map<OpenAiChannel, OpenAiGroupService> openAiGroup = new HashMap<>();

    public AbstractChatService(ChatGLMService chatGLMService, ChatGPTService chatGPTService) {
        openAiGroup.put(OpenAiChannel.ChatGLM, chatGLMService);
        openAiGroup.put(OpenAiChannel.ChatGPT, chatGPTService);
    }

    @Resource
    protected OpenAiSession openAiSession;

    @Resource
    private IOpenAiRepository openAiRepository;

    @Override
    public ResponseBodyEmitter completions(ResponseBodyEmitter emitter,ChatProcessAggregate chatProcess) throws Exception {
        // 1. 请求应答
        emitter.onCompletion(()->{
            log.info("流式问答请求完成，使用模型：{}", chatProcess.getModel());
        });
        emitter.onError(throwable -> log.error("流式问答请求疫情，使用模型：{}", chatProcess.getModel(), throwable));

        // 用户账户获取
        UserAccountQuotaEntity userAccountQuotaEntity = openAiRepository.queryUserAccount(chatProcess.getOpenId());

         // 2. 规则过滤
        RuleLogicEntity<ChatProcessAggregate> ruleLogicEntity = this.doCheckLogic(chatProcess,
                userAccountQuotaEntity,
                DefaultLogicFactory.LogicModel.ACCESS_LIMIT.getCode(),
                DefaultLogicFactory.LogicModel.SENSITIVE_WORD.getCode(),
                null != userAccountQuotaEntity ? DefaultLogicFactory.LogicModel.ACCOUNT_STATUS.getCode() : DefaultLogicFactory.LogicModel.NULL.getCode(),
                null != userAccountQuotaEntity ? DefaultLogicFactory.LogicModel.MODEL_TYPE.getCode() : DefaultLogicFactory.LogicModel.NULL.getCode(),
                null != userAccountQuotaEntity ? DefaultLogicFactory.LogicModel.USER_QUOTA.getCode() : DefaultLogicFactory.LogicModel.NULL.getCode());

        if (!LogicCheckTypeVO.SUCCESS.equals(ruleLogicEntity.getType())){
            emitter.send(ruleLogicEntity.getInfo());
            emitter.complete();
            return emitter;
        }

        // 3.应答处理
        try {
            // 4. 应答处理 【ChatGPT、ChatGLM 策略模式】
//            this.doMessageResponse(chatProcess, emitter);
            openAiGroup.get(chatProcess.getChannel()).doMessageResponse(ruleLogicEntity.getData(),emitter);
        } catch (Exception e) {
            throw new ChatGPTException(Constants.ResponseCode.UN_ERROR.getCode(), Constants.ResponseCode.UN_ERROR.getInfo());
        }
        // 4.返回结果
        return emitter;
    }

    protected abstract RuleLogicEntity<ChatProcessAggregate> doCheckLogic(ChatProcessAggregate chatProcess,UserAccountQuotaEntity userAccountQuotaEntity, String... logics) throws Exception;


    protected abstract void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter responseBodyEmitter) throws JsonProcessingException;
}
