package com.safe_jeonse.server.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public record JusoTokenResponse(
        @JsonProperty("result") ResultData result
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResultData(
            @JsonProperty("accessToken") String accessToken,
            @JsonProperty("accessTimeout") String accessTimeout
    ) {}
}
