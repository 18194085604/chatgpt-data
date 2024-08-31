package com.gjy.trigger.http;

import com.alibaba.fastjson.JSON;
import com.gjy.domain.openai.model.aggregates.ChatProcessAggregate;
import com.gjy.domain.openai.model.entity.MessageEntity;
import com.gjy.domain.openai.service.IChatService;
import com.gjy.trigger.http.dto.ChatGPTRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;

@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/${app.config.api-version}/")
public class ChatGPTAIServiceController {
    @Resource
    private IChatService chatService;

    @RequestMapping(value = "chat/completions", method = RequestMethod.POST)
    public ResponseBodyEmitter completionsStream(@RequestBody ChatGPTRequestDTO request, @RequestHeader("Authorization") String token, HttpServletResponse response) {

        log.info("流式问答请求开始，使用模型：{} 请求信息：{}", request.getModel(), JSON.toJSONString(request.getMessages()));
        // 1. 基础配置；流式输出、编码、禁用缓存
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        // 2. 构建参数
        ChatProcessAggregate chatProcessAggregate = ChatProcessAggregate.builder()
                .token(token)
                .model(request.getModel())
                .messages(request.getMessages().stream()
                        .map(entity -> MessageEntity.builder()
                                .role(entity.getRole())
                                .content(entity.getContent())
                                .name(entity.getName())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        // 3. 请求结果&返回
        return chatService.completions(chatProcessAggregate);
    }
}
