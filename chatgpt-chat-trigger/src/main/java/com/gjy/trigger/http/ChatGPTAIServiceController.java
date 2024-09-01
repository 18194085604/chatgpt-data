package com.gjy.trigger.http;

import com.alibaba.fastjson.JSON;
import com.gjy.domain.auth.service.IAuthService;
import com.gjy.domain.openai.model.aggregates.ChatProcessAggregate;
import com.gjy.domain.openai.model.entity.MessageEntity;
import com.gjy.domain.openai.service.IChatService;
import com.gjy.trigger.http.dto.ChatGPTRequestDTO;
import com.gjy.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/${app.config.api-version}/")
public class ChatGPTAIServiceController {
    @Resource
    private IChatService chatService;

    @Resource
    private IAuthService authService;

    @RequestMapping(value = "chat/completions", method = RequestMethod.POST)
    public ResponseBodyEmitter completionsStream(@RequestBody ChatGPTRequestDTO request, @RequestHeader("Authorization") String token, HttpServletResponse response) throws Exception {

        log.info("流式问答请求开始，使用模型：{} 请求信息：{}", request.getModel(), JSON.toJSONString(request.getMessages()));
        // 1. 基础配置；流式输出、编码、禁用缓存
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        // 2. 构建异步响应对象【对 Token 过期拦截】
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(3 * 60 * 1000L);
        boolean success = authService.checkToken(token);
        if (!success) {
            try {
                emitter.send(Constants.ResponseCode.TOKEN_ERROR.getCode());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            emitter.complete();
            return emitter;
        }

        // 3. 获取 OpenID
        String openid = authService.openid(token);
        log.info("流式问答请求处理，openid:{} 请求模型:{}", openid, request.getModel());


        // 4. 构建参数
        ChatProcessAggregate chatProcessAggregate = ChatProcessAggregate.builder()
                .openId(openid)
                .model(request.getModel())
                .messages(request.getMessages().stream()
                        .map(entity -> MessageEntity.builder()
                                .role(entity.getRole())
                                .content(entity.getContent())
                                .name(entity.getName())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        // 5. 请求结果&返回
        return chatService.completions(emitter,chatProcessAggregate);
    }
}
