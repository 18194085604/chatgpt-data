package com.gjy.test.domain.openai;

import com.gjy.domain.openai.model.aggregates.ChatProcessAggregate;
import com.gjy.domain.openai.model.entity.MessageEntity;
import com.gjy.domain.openai.service.IChatService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ChatServiceTest {

    @Resource
    private IChatService chatService;
    @Test
    public void test_completions() throws Exception {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter();

        ChatProcessAggregate chatProcessAggregate = new ChatProcessAggregate();
        chatProcessAggregate.setOpenId("xfg");
        chatProcessAggregate.setModel("gpt-3.5-turbo");
        chatProcessAggregate.setMessages(Collections.singletonList(MessageEntity.builder().role("user").content("1+1").build()));

        ResponseBodyEmitter completions = chatService.completions(emitter, chatProcessAggregate);

        // 等待
        new CountDownLatch(1).await();

    }

}
