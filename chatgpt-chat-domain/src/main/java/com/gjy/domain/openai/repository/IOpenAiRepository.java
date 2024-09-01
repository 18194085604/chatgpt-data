package com.gjy.domain.openai.repository;

import com.gjy.domain.openai.model.entity.UserAccountQuotaEntity;

public interface IOpenAiRepository {

    int subAccountQuota(String openai);

    UserAccountQuotaEntity queryUserAccount(String openid);

}
