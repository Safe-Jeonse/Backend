package com.safe_jeonse.server.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.List;

/**
 * 다가구 주택 가격 조회 API 응답 DTO
 */
public record DagaguPriceResponse(
        @JsonProperty("indvdHousingPrices") IndvdHousingPrices indvdHousingPrices
) {
    public IndvdHousingPrices getIndvdHousingPrices() { return indvdHousingPrices; }

    public static record IndvdHousingPrices(
            @JsonProperty("field")
            @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            List<Field> field
    ) {
        public List<Field> getField() { return field; }
    }

    public static record Field(
            @JsonProperty("calcPlotAr") String calcPlotAr,
            @JsonProperty("stdrYear") String stdrYear,
            @JsonProperty("ldCode") String ldCode,
            @JsonProperty("ldCodeNm") String ldCodeNm,
            @JsonProperty("bildRegstrEsntlNo") String bildRegstrEsntlNo,
            @JsonProperty("housePc") String housePc,
            @JsonProperty("buldCalcTotAr") String buldCalcTotAr,
            @JsonProperty("mnnmSlno") String mnnmSlno,
            @JsonProperty("stdLandAt") String stdLandAt,
            @JsonProperty("ladRegstrAr") String ladRegstrAr,
            @JsonProperty("pnu") String pnu,
            @JsonProperty("dongCode") String dongCode,
            @JsonProperty("lastUpdtDt") String lastUpdtDt,
            @JsonProperty("regstrSeCodeNm") String regstrSeCodeNm,
            @JsonProperty("stdrMt") String stdrMt,
            @JsonProperty("regstrSeCode") String regstrSeCode,
            @JsonProperty("buldAllTotAr") String buldAllTotAr
    ) {
        public String getCalcPlotAr() { return calcPlotAr; }
        public String getStdrYear() { return stdrYear; }
        public String getLdCode() { return ldCode; }
        public String getLdCodeNm() { return ldCodeNm; }
        public String getBildRegstrEsntlNo() { return bildRegstrEsntlNo; }
        public String getHousePc() { return housePc; }
        public String getBuldCalcTotAr() { return buldCalcTotAr; }
        public String getMnnmSlno() { return mnnmSlno; }
        public String getStdLandAt() { return stdLandAt; }
        public String getLadRegstrAr() { return ladRegstrAr; }
        public String getPnu() { return pnu; }
        public String getDongCode() { return dongCode; }
        public String getLastUpdtDt() { return lastUpdtDt; }
        public String getRegstrSeCodeNm() { return regstrSeCodeNm; }
        public String getStdrMt() { return stdrMt; }
        public String getRegstrSeCode() { return regstrSeCode; }
        public String getBuldAllTotAr() { return buldAllTotAr; }

        /**
         * 주택 가격을 Long 타입으로 반환
         */
        public Long getHousePriceAsLong() {
            try {
                return (housePc != null && !housePc.trim().isEmpty())
                    ? Long.parseLong(housePc.trim())
                    : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        /**
         * 건물 연면적을 Double 타입으로 반환
         */
        public Double getBuldCalcTotArAsDouble() {
            try {
                return (buldCalcTotAr != null && !buldCalcTotAr.trim().isEmpty())
                    ? Double.parseDouble(buldCalcTotAr.trim())
                    : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
}

