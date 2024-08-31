package com.gjy.domain.weixin.service.message;

import com.gjy.domain.weixin.model.entity.UserBehaviorMessageEntity;
import com.gjy.domain.weixin.service.IWeiXinBehaviorService;
import org.springframework.stereotype.Service;

@Service
public class WeiXinBehaviorService implements IWeiXinBehaviorService {
    @Override
    public String acceptUserBehavior(UserBehaviorMessageEntity userBehaviorMessageEntity) {
        return "";
    }
}
