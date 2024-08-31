package com.gjy.domain.openai.model.aggregates;

import com.gjy.domain.openai.model.entity.MessageEntity;
import com.gjy.types.enums.ChatGPTModel;
import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class ChatProcessAggregate {
    private String token;
    private String model = ChatGPTModel.GPT_3_5_TURBO.getCode();
    private List<MessageEntity> messages;
}
