package com.gjy.infrastructure.adapter.dao;

import com.gjy.infrastructure.adapter.po.UserAccountPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IUserAccountDao {

    int subAccountQuota(String openid);

    UserAccountPO queryUserAccount(String openid);

}
