package com.gjy.domain.order.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayTypeVO {

    ZHIFUBAO_NATIVE(0, "支付宝Native支付"),
            ;

    private final Integer code;
    private final String desc;

    public static PayTypeVO get(Integer code){
        switch (code){
            case 0:
                return PayTypeVO.ZHIFUBAO_NATIVE;
            default:
                return PayTypeVO.ZHIFUBAO_NATIVE;
        }
    }

}
