package com.gjy.domain.openai.service;

import com.gjy.domain.openai.model.aggregates.ChatProcessAggregate;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

public interface IChatService {
    ResponseBodyEmitter completions(ChatProcessAggregate chatProcessAggregate);
}
