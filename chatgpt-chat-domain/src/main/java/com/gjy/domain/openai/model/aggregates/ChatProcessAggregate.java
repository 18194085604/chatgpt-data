package com.gjy.domain.openai.model.aggregates;

import com.gjy.domain.openai.model.entity.MessageEntity;
import com.gjy.types.common.Constants;
import com.gjy.types.enums.ChatGPTModel;
import com.gjy.types.enums.OpenAiChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ChatProcessAggregate {
    // 用户id
    private String openId;
    private String model = ChatGPTModel.GPT_3_5_TURBO.getCode();
    private List<MessageEntity> messages;


    public boolean isWhiteList(String whiteListStr) {
        String[] whiteList = whiteListStr.split(Constants.SPLIT);
        for (String whiteOpenid : whiteList) {
            if (whiteOpenid.equals(openId)) return true;
        }
        return false;
    }

    public OpenAiChannel getChannel(){
        return OpenAiChannel.getChannel(this.model);
    }
}
