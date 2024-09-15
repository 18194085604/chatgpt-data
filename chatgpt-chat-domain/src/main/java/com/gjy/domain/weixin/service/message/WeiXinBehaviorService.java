package com.gjy.domain.weixin.service.message;

import com.gjy.domain.weixin.model.entity.MessageTextEntity;
import com.gjy.domain.weixin.model.entity.UserBehaviorMessageEntity;
import com.gjy.domain.weixin.model.valobj.MsgTypeVO;
import com.gjy.domain.weixin.service.IWeiXinBehaviorService;
import com.gjy.types.exception.ChatGPTException;
import com.gjy.types.sdk.weixin.XmlUtil;
import com.google.common.cache.Cache;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class WeiXinBehaviorService implements IWeiXinBehaviorService {
    @Resource
    private Cache<String, String> codeCache;

    @Value("${wx.config.originalid}")
    private String originalId;


    @Override
    public synchronized String acceptUserBehavior(UserBehaviorMessageEntity userBehaviorMessageEntity) {
        // 事件类型，忽略处理
        if (MsgTypeVO.EVENT.getType().equals(userBehaviorMessageEntity.getMsgType())) {
            return "";
        }
        // Text 文本类型
        if (MsgTypeVO.TEXT.getType().equals(userBehaviorMessageEntity.getMsgType())) {
            // 缓存验证码
            String isExistCode = codeCache.getIfPresent(userBehaviorMessageEntity.getOpenId());

            // 判断验证码 - 不考虑验证码重复问题
            if (StringUtils.isBlank(isExistCode)){
                String code = RandomStringUtils.randomNumeric(4);
                codeCache.put(code, userBehaviorMessageEntity.getOpenId());
                codeCache.put(userBehaviorMessageEntity.getOpenId(), code);
                isExistCode = code;
            }

            // 反馈信息[文本]
            MessageTextEntity res = new MessageTextEntity();
            res.setToUserName(userBehaviorMessageEntity.getOpenId());
            res.setFromUserName(originalId);
            res.setCreateTime(String.valueOf(System.currentTimeMillis() / 1000L));
            res.setMsgType("text");
            res.setContent(String.format("您的验证码为：%s 有效期%d分钟！", isExistCode, 3));
            return XmlUtil.beanToXml(res);
        }
        throw new ChatGPTException(userBehaviorMessageEntity.getMsgType() + "未被处理的行为类型 Err！");
    }
}
