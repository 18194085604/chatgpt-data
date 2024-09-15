package com.gjy.infrastructure.dao;

import com.gjy.infrastructure.po.UserAccountPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IUserAccountDao {

    int subAccountQuota(String openid);

    UserAccountPO queryUserAccount(String openid);

    int addAccountQuota(UserAccountPO userAccountPOReq);

    void insert(UserAccountPO userAccountPOReq);
}
