package com.gjy.domain.weixin.service;

import com.gjy.domain.weixin.model.entity.UserBehaviorMessageEntity;

public interface IWeiXinBehaviorService {
    String acceptUserBehavior(UserBehaviorMessageEntity userBehaviorMessageEntity);
}
