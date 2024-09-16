package com.gjy.config;

import cn.bugstack.chatglm.session.OpenAiSession;
import cn.bugstack.chatglm.session.OpenAiSessionFactory;
import cn.bugstack.chatglm.session.defaults.DefaultOpenAiSessionFactory;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ChatGLMSDKConfigProperties.class)
public class ChatGLMSDKConfig {

    @Bean
    @ConditionalOnProperty(value = "chatgptglm.sdk.config.enabled",havingValue = "true",matchIfMissing = false)
    public OpenAiSession openAiSession(ChatGLMSDKConfigProperties chatGPTSDKConfigProperties) {
        // 1.配置文件
        cn.bugstack.chatglm.session.Configuration configuration = new cn.bugstack.chatglm.session.Configuration();
        configuration.setApiHost(chatGPTSDKConfigProperties.getApiHost());
        configuration.setApiSecretKey(chatGPTSDKConfigProperties.getApiKey());
        configuration.setLevel(HttpLoggingInterceptor.Level.BODY);
        // 2.会话工厂
        OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);
        // 3.开启会话
        return factory.openSession();
    }
}
