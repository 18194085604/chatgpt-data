package com.gjy.trigger.http;

import cn.bugstack.chatglm.model.ChatCompletionRequest;
import cn.bugstack.chatglm.model.ChatCompletionResponse;
import cn.bugstack.chatglm.model.Model;
import cn.bugstack.chatglm.model.Role;
import cn.bugstack.chatglm.session.OpenAiSession;
import com.alibaba.fastjson.JSON;
import com.gjy.trigger.http.dto.ChatGPTRequestDTO;
import com.gjy.types.exception.ChatGPTException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1")
public class ChatGPTAIServiceControllerOld {

    @Resource
    private OpenAiSession openAiSession;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;


    @RequestMapping(value = "chat/completions", method = RequestMethod.POST)
    public ResponseBodyEmitter completionsStream(@RequestBody ChatGPTRequestDTO request, @RequestHeader("Authorization")String token, HttpServletResponse response) throws Exception {
        log.info("流式问答请求开始，使用模型：{} 请求信息：{}", request.getModel(), JSON.toJSONString(request.getMessages()));


        try {
            // 1. 基础配置；流式输出、编码、禁用缓存
            response.setContentType("text/event-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");

//            if (!token.equals("b8b6")) throw new RuntimeException("token err!");

            // 2. 异步处理 HTTP 响应处理类
            ResponseBodyEmitter emitter = new ResponseBodyEmitter(3 * 60 * 1000L);
            emitter.onCompletion(() -> {
                log.info("流式问答请求完成，使用模型：{}", request.getModel());
            });
            emitter.onError(throwable -> log.error("流式问答请求疫情，使用模型：{}", request.getModel(), throwable));

            // 3.1 构建参数
            // 入参；模型、请求信息
            ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
            chatCompletionRequest.setModel(Model.GLM_3_5_TURBO);
            chatCompletionRequest.setIncremental(false);
            chatCompletionRequest.setIsCompatible(true); // 是否对返回结果数据做兼容，24年1月发布的 GLM_3_5_TURBO、GLM_4 模型，与之前的模型在返回结果上有差异。开启 true 可以做兼容。
            chatCompletionRequest.setTools(new ArrayList<ChatCompletionRequest.Tool>() {
                private static final long serialVersionUID = -7988151926241837899L;

                {
                    add(ChatCompletionRequest.Tool.builder()
                            .type(ChatCompletionRequest.Tool.Type.web_search)
                            .webSearch(ChatCompletionRequest.Tool.WebSearch.builder().enable(true).searchQuery(request.getMessages().get(0).getContent()).build())
                            .build());
                }
            });
            chatCompletionRequest.setPrompt(new ArrayList<ChatCompletionRequest.Prompt>() {
                private static final long serialVersionUID = -7988151926241837899L;

                {
                    add(ChatCompletionRequest.Prompt.builder()
                            .role(Role.user.getCode())
                            .content(request.getMessages().get(0).getContent())
                            .build());
                }
            });
            openAiSession.completions(chatCompletionRequest, new EventSourceListener() {

                @Override
                public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                    ChatCompletionResponse chatCompletionResponse = JSON.parseObject(data, ChatCompletionResponse.class);
                    List<ChatCompletionResponse.Choice> choices = chatCompletionResponse.getChoices();
                    for (ChatCompletionResponse.Choice chatChoice : choices) {
                        ChatCompletionResponse.Delta delta = chatChoice.getDelta();

                        // 应答完成
                        String finishReason = chatChoice.getFinishReason();
                        if (StringUtils.isNoneBlank(finishReason) && "stop".equals(finishReason)) {
                            emitter.complete();
                            break;
                        }

                        // 发送信息
                        try {
                            emitter.send(delta.getContent());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
            return emitter;
        } catch (Exception e) {
            log.error("流式应答，请求模型：{} 发生异常", request.getModel(), e);
            throw new ChatGPTException(e.getMessage());
        }


    }

    @RequestMapping(value = "/chat", method = RequestMethod.GET)
    public ResponseBodyEmitter completionsStream(HttpServletResponse response) {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        ResponseBodyEmitter emitter = new ResponseBodyEmitter();

        threadPoolExecutor.execute(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    emitter.send("strdddddddddddddddd\r\n" + i);
                    Thread.sleep(100);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            emitter.complete();
        });

        return emitter;
    }
}
