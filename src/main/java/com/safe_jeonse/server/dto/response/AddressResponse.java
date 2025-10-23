package com.safe_jeonse.server.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AddressResponse {
    @JsonProperty("results")
    private Results results;

    @Data
    public static class Results {
        @JsonProperty("common")
        private Common common;

        @JsonProperty("juso")
        private List<Juso> juso;

        public int getTotalCount() {
            return Integer.parseInt(common.getTotalCount());
        }
    }

    @Data
    public static class Common {
        @JsonProperty("totalCount")
        private String totalCount;

        @JsonProperty("currentPage")
        private int currentPage;

        @JsonProperty("countPerPage")
        private int countPerPage;

        @JsonProperty("errorCode")
        private String errorCode;

        @JsonProperty("errorMessage")
        private String errorMessage;
    }

    @Data
    public static class Juso {
        @JsonProperty("roadAddr")
        private String roadAddr;

        @JsonProperty("roadAddrPart1")
        private String roadAddrPart1;

        @JsonProperty("roadAddrPart2")
        private String roadAddrPart2;

        @JsonProperty("jibunAddr")
        private String jibunAddr;

        @JsonProperty("zipNo")
        private String zipNo;

        @JsonProperty("bdNm")
        private String buildingName;

        @JsonProperty("siNm")
        private String siName;

        @JsonProperty("sggNm")
        private String sggName;

        @JsonProperty("emdNm")
        private String emdName;
    }
}