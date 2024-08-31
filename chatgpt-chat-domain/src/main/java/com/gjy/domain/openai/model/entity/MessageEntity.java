package com.gjy.domain.openai.model.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageEntity {
    private String role;
    private String content;
    private String name;
}
