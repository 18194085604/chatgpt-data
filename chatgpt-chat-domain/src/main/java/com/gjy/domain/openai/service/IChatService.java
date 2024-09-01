package com.gjy.domain.openai.service;

import com.gjy.domain.openai.model.aggregates.ChatProcessAggregate;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

public interface IChatService {
    ResponseBodyEmitter completions(ResponseBodyEmitter emitter,ChatProcessAggregate chatProcessAggregate) throws Exception;
}
