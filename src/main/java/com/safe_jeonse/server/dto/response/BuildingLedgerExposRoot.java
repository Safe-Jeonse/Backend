package com.safe_jeonse.server.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BuildingLedgerExposRoot(
        @JsonProperty("response") BuildingLedgerExposResponse response
) {
    public BuildingLedgerExposResponse getResponse() { return response; }
}

