package com.gjy.domain.weixin;

public interface IWeiXinValidateService {
    boolean checkSign(String signature, String timestamp, String nonce);
}
