package com.safe_jeonse.server.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BuildingLedgerTitleRoot(
        @JsonProperty("response") BuildingLedgerTitleResponse response
) {
    public BuildingLedgerTitleResponse getResponse() { return response; }
}

