package com.gjy.domain.auth.service;

import com.gjy.domain.auth.model.entity.AuthStateEntity;

public interface IAuthService {

    /**
     * 登录验证
     * @param code 验证码
     * @return Token
     */
    AuthStateEntity doLogin(String code);

    boolean checkToken(String token);

    String openid(String token);
}
