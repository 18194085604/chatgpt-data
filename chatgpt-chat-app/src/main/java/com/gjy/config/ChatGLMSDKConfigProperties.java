package com.gjy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
@Data
@ConfigurationProperties(prefix = "chatgptglm.sdk.config")
public class ChatGLMSDKConfigProperties {

    private String apiHost;

    private String apiKey;
}
