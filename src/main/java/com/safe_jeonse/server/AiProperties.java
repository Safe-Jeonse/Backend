package com.safe_jeonse.server;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "openai")
@Getter
public class AiProperties {

    public String apiKey;
    public String model;

}
