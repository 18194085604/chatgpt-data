package com.gjy.domain.openai.service;

import cn.bugstack.chatglm.model.ChatCompletionRequest;
import cn.bugstack.chatglm.model.ChatCompletionResponse;
import cn.bugstack.chatglm.model.Model;
import cn.bugstack.chatglm.model.Role;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gjy.domain.openai.model.aggregates.ChatProcessAggregate;
import com.gjy.domain.openai.model.entity.RuleLogicEntity;
import com.gjy.domain.openai.model.entity.UserAccountQuotaEntity;
import com.gjy.domain.openai.model.valobj.LogicCheckTypeVO;
import com.gjy.domain.openai.service.channel.impl.ChatGLMService;
import com.gjy.domain.openai.service.channel.impl.ChatGPTService;
import com.gjy.domain.openai.service.rule.ILogicFilter;
import com.gjy.domain.openai.service.rule.factory.DefaultLogicFactory;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ChatService extends AbstractChatService {

    @Resource
    private DefaultLogicFactory defaultLogicFactory;

    public ChatService(ChatGLMService chatGLMService, ChatGPTService chatGPTService) {
        super(chatGLMService, chatGPTService);
    }

    @Override
    protected void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter responseBodyEmitter) throws JsonProcessingException {

        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setModel(Model.GLM_3_5_TURBO);
        chatCompletionRequest.setIncremental(false);
        chatCompletionRequest.setIsCompatible(true); // 是否对返回结果数据做兼容，24年1月发布的 GLM_3_5_TURBO、GLM_4 模型，与之前的模型在返回结果上有差异。开启 true 可以做兼容。
        chatCompletionRequest.setTools(new ArrayList<ChatCompletionRequest.Tool>() {
            private static final long serialVersionUID = -7988151926241837899L;

            {
                add(ChatCompletionRequest.Tool.builder()
                        .type(ChatCompletionRequest.Tool.Type.web_search)
                        .webSearch(ChatCompletionRequest.Tool.WebSearch.builder().enable(true).searchQuery(chatProcess.getMessages().get(0).getContent()).build())
                        .build());
            }
        });
        chatCompletionRequest.setPrompt(new ArrayList<ChatCompletionRequest.Prompt>() {
            private static final long serialVersionUID = -7988151926241837899L;

            {
                add(ChatCompletionRequest.Prompt.builder()
                        .role(Role.user.getCode())
                        .content(chatProcess.getMessages().get(0).getContent())
                        .build());
            }
        });

        try {
            openAiSession.completions(chatCompletionRequest, new EventSourceListener() {
                @Override
                public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                    ChatCompletionResponse chatCompletionResponse = JSON.parseObject(data, ChatCompletionResponse.class);
                    List<ChatCompletionResponse.Choice> choices = chatCompletionResponse.getChoices();
                    for (ChatCompletionResponse.Choice choice : choices) {
                        ChatCompletionResponse.Delta delta = choice.getDelta();

                        // 应答完成
                        String finishReason = choice.getFinishReason();
                        if (StringUtils.isNoneBlank(finishReason) && "stop".equals(finishReason)) {
                            responseBodyEmitter.complete();
                            break;
                        }


                        // 发送信息
                        try {
                            responseBodyEmitter.send(delta.getContent());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected RuleLogicEntity<ChatProcessAggregate> doCheckLogic(ChatProcessAggregate chatProcess, UserAccountQuotaEntity userAccountQuotaEntity, String... logics) throws Exception {
        Map<String, ILogicFilter> map = defaultLogicFactory.openLogicFilter();
        RuleLogicEntity<ChatProcessAggregate> entity = null;
        for (String logic : logics) {
            if (DefaultLogicFactory.LogicModel.NULL.getCode().equals(logic)) continue;
            entity = map.get(logic).filter(chatProcess, userAccountQuotaEntity);
            if (!LogicCheckTypeVO.SUCCESS.equals(entity.getType())) return entity;
        }
        return entity != null ? entity : RuleLogicEntity.<ChatProcessAggregate>builder()
                .type(LogicCheckTypeVO.SUCCESS).data(chatProcess).build();
    }

}

