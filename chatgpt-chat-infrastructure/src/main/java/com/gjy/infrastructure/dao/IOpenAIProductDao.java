package com.gjy.infrastructure.dao;

import com.gjy.infrastructure.po.OpenAIProductPO;
import org.apache.ibatis.annotations.Mapper;

import javax.xml.ws.soap.MTOM;
import java.util.List;
@Mapper
public interface IOpenAIProductDao {
    List<OpenAIProductPO> queryProductList();
}
