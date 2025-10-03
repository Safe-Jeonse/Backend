package com.safe_jeonse.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeepCheckService {

    public String deepCheck() {
        return "deep";
    }
}
