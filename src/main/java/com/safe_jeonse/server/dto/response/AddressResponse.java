package com.safe_jeonse.server.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AddressResponse(
        @JsonProperty("results") Results results
) {
    public Results getResults() {
        return results;
    }

    public record Results(
            @JsonProperty("common") Common common,
            @JsonProperty("juso") List<Juso> juso
    ) {
        public int getTotalCount() {
            try {
                return Integer.parseInt(common.getTotalCount());
            } catch (Exception e) {
                return 0;
            }
        }

        public Common getCommon() {
            return common;
        }

        public List<Juso> getJuso() {
            return juso;
        }
    }

    public record Common(
            @JsonProperty("totalCount") String totalCount,
            @JsonProperty("currentPage") int currentPage,
            @JsonProperty("countPerPage") int countPerPage,
            @JsonProperty("errorCode") String errorCode,
            @JsonProperty("errorMessage") String errorMessage
    ) {
        public String getTotalCount() {
            return totalCount;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public int getCountPerPage() {
            return countPerPage;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public record Juso(
            @JsonProperty("roadAddr") String roadAddr,
            @JsonProperty("roadAddrPart1") String roadAddrPart1,
            @JsonProperty("roadAddrPart2") String roadAddrPart2,
            @JsonProperty("jibunAddr") String jibunAddr,
            @JsonProperty("zipNo") String zipNo,
            @JsonProperty("bdNm") String buildingName,
            @JsonProperty("siNm") String siName,
            @JsonProperty("sggNm") String sggName,
            @JsonProperty("emdNm") String emdName,
            @JsonProperty("liNm") String liNm,
            @JsonProperty("rn") String rn,
            @JsonProperty("udrtYn") String udrtYn,
            @JsonProperty("buldMnnm") String buldMnnm,
            @JsonProperty("buldSlno") String buldSlno,
            @JsonProperty("mtYn") String mtYn,
            @JsonProperty("lnbrMnnm") String lnbrMnnm,
            @JsonProperty("lnbrSlno") String lnbrSlno,
            @JsonProperty("emdNo") String emdNo
    ) {
        public String getRoadAddr() { return roadAddr; }
        public String getRoadAddrPart1() { return roadAddrPart1; }
        public String getRoadAddrPart2() { return roadAddrPart2; }
        public String getJibunAddr() { return jibunAddr; }
        public String getZipNo() { return zipNo; }
        public String getBuildingName() { return buildingName; }
        public String getSiName() { return siName; }
        public String getSggName() { return sggName; }
        public String getEmdName() { return emdName; }

        public String getLiNm() { return liNm; }
        public String getRn() { return rn; }
        public String getUdrtYn() { return udrtYn; }
        public String getBuldMnnm() { return buldMnnm; }
        public String getBuldSlno() { return buldSlno; }
        public String getMtYn() { return mtYn; }
        public String getLnbrMnnm() { return lnbrMnnm; }
        public String getLnbrSlno() { return lnbrSlno; }
        public String getEmdNo() { return emdNo; }
    }
}