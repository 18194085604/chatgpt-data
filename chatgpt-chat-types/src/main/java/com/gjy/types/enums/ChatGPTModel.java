package com.gjy.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ChatGPTModel {

    GPT_3_5_TURBO("gpt-3-5-turbo"),

    ;

    private final String code;
}
