package com.safe_jeonse.server.service;

import com.safe_jeonse.server.AiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeepCheckService {

    private final AiProperties aiProperties;

    public String deepCheck() {

        String apiKey = aiProperties.getApiKey();
        String model = aiProperties.getModel();

        return "deep";
    }
}
