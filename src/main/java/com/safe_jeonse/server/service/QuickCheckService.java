package com.safe_jeonse.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuickCheckService {

    public String quickCheck() {
        return "quick";
    }
}
