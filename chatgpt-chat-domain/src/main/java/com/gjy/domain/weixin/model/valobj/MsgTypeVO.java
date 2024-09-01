package com.gjy.domain.weixin.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.checkerframework.checker.units.qual.A;


@Getter
@AllArgsConstructor
public enum MsgTypeVO {

    EVENT("event","事件"),
    TEXT("text","文本"),
    ;

    private String code;
    private String type;
}
