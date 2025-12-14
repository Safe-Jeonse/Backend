package com.safe_jeonse.server.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * AI 아파트 가격 조회 응답 DTO
 */
@Builder
public record AiApartmentPriceResponse(
        @JsonProperty("averagePrice")
        Long averagePrice,

        @JsonProperty("confidence")
        String confidence,

        @JsonProperty("dataSource")
        String dataSource,

        @JsonProperty("transactionCount")
        Integer transactionCount,

        @JsonProperty("priceRange")
        PriceRange priceRange,

        @JsonProperty("note")
        String note
) {
    public record PriceRange(
            @JsonProperty("min")
            Long min,

            @JsonProperty("max")
            Long max
    ) {
        // null 안전 기본값 제공
        public PriceRange {
            min = (min != null) ? min : 0L;
            max = (max != null) ? max : 0L;
        }
    }

    // null 안전 기본값 제공
    public AiApartmentPriceResponse {
        averagePrice = (averagePrice != null) ? averagePrice : 0L;
        confidence = (confidence != null && !confidence.isBlank()) ? confidence : "none";
        dataSource = (dataSource != null && !dataSource.isBlank()) ? dataSource : "알 수 없음";
        transactionCount = (transactionCount != null) ? transactionCount : 0;
        priceRange = (priceRange != null) ? priceRange : new PriceRange(0L, 0L);
        note = (note != null && !note.isBlank()) ? note : "";
    }

    /**
     * confidence가 "none"인지 확인
     */
    public boolean hasNoData() {
        return averagePrice == 0L || "none".equalsIgnoreCase(confidence);
    }

    /**
     * 신뢰도가 충분한지 확인 (high 또는 medium)
     */
    public boolean isReliable() {
        return !"none".equalsIgnoreCase(confidence) && !"low".equalsIgnoreCase(confidence);
    }
}
