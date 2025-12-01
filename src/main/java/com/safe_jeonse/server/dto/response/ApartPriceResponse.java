package com.safe_jeonse.server.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.List;

/**
 * 아파트 공시가격 조회 API 응답 DTO
 */
public record ApartPriceResponse(
        @JsonProperty("apartHousingPrices") ApartHousingPrices apartHousingPrices
) {
    public ApartHousingPrices getApartHousingPrices() { return apartHousingPrices; }

    public record ApartHousingPrices(
            @JsonProperty("field")
            @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            List<Field> field
    ) {
        public List<Field> getField() { return field; }
    }

    public record Field(
            @JsonProperty("stdrYear") String stdrYear,
            @JsonProperty("aphusSeCode") String aphusSeCode,
            @JsonProperty("prvuseAr") String prvuseAr,
            @JsonProperty("ldCode") String ldCode,
            @JsonProperty("ldCodeNm") String ldCodeNm,
            @JsonProperty("spclLandNm") String spclLandNm,
            @JsonProperty("pblntfPc") String pblntfPc,
            @JsonProperty("mnnmSlno") String mnnmSlno,
            @JsonProperty("aphusCode") String aphusCode,
            @JsonProperty("pnu") String pnu,
            @JsonProperty("hoNm") String hoNm,
            @JsonProperty("dongNm") String dongNm,
            @JsonProperty("lastUpdtDt") String lastUpdtDt,
            @JsonProperty("regstrSeCodeNm") String regstrSeCodeNm,
            @JsonProperty("stdrMt") String stdrMt,
            @JsonProperty("regstrSeCode") String regstrSeCode,
            @JsonProperty("floorNm") String floorNm,
            @JsonProperty("aphusNm") String aphusNm,
            @JsonProperty("aphusSeCodeNm") String aphusSeCodeNm
    ) {
        public String getStdrYear() { return stdrYear; }
        public String getAphusSeCode() { return aphusSeCode; }
        public String getPrvuseAr() { return prvuseAr; }
        public String getLdCode() { return ldCode; }
        public String getLdCodeNm() { return ldCodeNm; }
        public String getSpclLandNm() { return spclLandNm; }
        public String getPblntfPc() { return pblntfPc; }
        public String getMnnmSlno() { return mnnmSlno; }
        public String getAphusCode() { return aphusCode; }
        public String getPnu() { return pnu; }
        public String getHoNm() { return hoNm; }
        public String getDongNm() { return dongNm; }
        public String getLastUpdtDt() { return lastUpdtDt; }
        public String getRegstrSeCodeNm() { return regstrSeCodeNm; }
        public String getStdrMt() { return stdrMt; }
        public String getRegstrSeCode() { return regstrSeCode; }
        public String getFloorNm() { return floorNm; }
        public String getAphusNm() { return aphusNm; }
        public String getAphusSeCodeNm() { return aphusSeCodeNm; }

        /**
         * 공시가격을 Long 타입으로 반환
         */
        public Long getPblntfPcAsLong() {
            try {
                return (pblntfPc != null && !pblntfPc.trim().isEmpty())
                    ? Long.parseLong(pblntfPc.trim())
                    : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        /**
         * 전용면적을 Double 타입으로 반환
         */
        public Double getPrvuseArAsDouble() {
            try {
                return (prvuseAr != null && !prvuseAr.trim().isEmpty())
                    ? Double.parseDouble(prvuseAr.trim())
                    : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
}

