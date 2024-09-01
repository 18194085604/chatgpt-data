package com.gjy.domain.auth.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class AuthStateEntity {
    private String code;
    private String info;
    private String openId;
    private String token;

}
