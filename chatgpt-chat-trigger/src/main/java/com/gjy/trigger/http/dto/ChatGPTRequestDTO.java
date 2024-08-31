package com.gjy.trigger.http.dto;

import com.gjy.types.enums.ChatGPTModel;
import lombok.Data;

import java.util.List;
@Data
public class ChatGPTRequestDTO {
    // 默认模型
    private String model = ChatGPTModel.GPT_3_5_TURBO.getCode();
    // 问题描述
    private List<MessageEntity> messages;
}
