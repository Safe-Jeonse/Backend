package com.safe_jeonse.server.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.List;

/*
   건축물대장 전유부 응답 DTO
 */
public record BuildingLedgerExposResponse(
        @JsonProperty("header") Header header,
        @JsonProperty("body") Body body
) {
    public Header getHeader() { return header; }
    public Body getBody() { return body; }

    public static record Header(
            @JsonProperty("resultCode") String resultCode,
            @JsonProperty("resultMsg") String resultMsg
    ) {
        public String getResultCode() { return resultCode; }
        public String getResultMsg() { return resultMsg; }
    }

    public static record Body(
            @JsonProperty("items") Items items,
            @JsonProperty("numOfRows") int numOfRows,
            @JsonProperty("pageNo") int pageNo,
            @JsonProperty("totalCount") int totalCount
    ) {
        public Items getItems() { return items; }
        public int getNumOfRows() { return numOfRows; }
        public int getPageNo() { return pageNo; }
        public int getTotalCount() { return totalCount; }
    }

    public static record Items(
            @JsonProperty("item")
            @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            List<Item> item
    ) {
        public List<Item> getItem() { return item; }
    }

    public static record Item(
            @JsonProperty("regstrGbCd") String regstrGbCd,
            @JsonProperty("regstrGbCdNm") String regstrGbCdNm,
            @JsonProperty("regstrKindCd") String regstrKindCd,
            @JsonProperty("regstrKindCdNm") String regstrKindCdNm,
            @JsonProperty("newPlatPlc") String newPlatPlc,
            @JsonProperty("bldNm") String bldNm,
            @JsonProperty("splotNm") String splotNm,
            @JsonProperty("block") String block,
            @JsonProperty("lot") String lot,
            @JsonProperty("naRoadCd") String naRoadCd,
            @JsonProperty("naBjdongCd") String naBjdongCd,
            @JsonProperty("naUgrndCd") String naUgrndCd,
            @JsonProperty("naMainBun") String naMainBun,
            @JsonProperty("naSubBun") String naSubBun,
            @JsonProperty("dongNm") String dongNm,
            @JsonProperty("hoNm") String hoNm,
            @JsonProperty("flrGbCd") String flrGbCd,
            @JsonProperty("flrGbCdNm") String flrGbCdNm,
            @JsonProperty("flrNo") String flrNo,
            @JsonProperty("flrNoNm") String flrNoNm,
            @JsonProperty("exposPubuseGbCd") String exposPubuseGbCd,
            @JsonProperty("exposPubuseGbCdNm") String exposPubuseGbCdNm,
            @JsonProperty("mainAtchGbCd") String mainAtchGbCd,
            @JsonProperty("mainAtchGbCdNm") String mainAtchGbCdNm,
            @JsonProperty("strctCd") String strctCd,
            @JsonProperty("strctCdNm") String strctCdNm,
            @JsonProperty("etcStrct") String etcStrct,
            @JsonProperty("mainPurpsCd") String mainPurpsCd,
            @JsonProperty("mainPurpsCdNm") String mainPurpsCdNm,
            @JsonProperty("etcPurps") String etcPurps,
            @JsonProperty("area") String area,
            @JsonProperty("crtnDay") String crtnDay,
            @JsonProperty("rnum") String rnum,
            @JsonProperty("platPlc") String platPlc,
            @JsonProperty("sigunguCd") String sigunguCd,
            @JsonProperty("bjdongCd") String bjdongCd,
            @JsonProperty("platGbCd") String platGbCd,
            @JsonProperty("bun") String bun,
            @JsonProperty("ji") String ji,
            @JsonProperty("mgmBldrgstPk") String mgmBldrgstPk
    ) {
        public String getRegstrGbCd() { return regstrGbCd; }
        public String getRegstrGbCdNm() { return regstrGbCdNm; }
        public String getRegstrKindCd() { return regstrKindCd; }
        public String getRegstrKindCdNm() { return regstrKindCdNm; }
        public String getNewPlatPlc() { return newPlatPlc; }
        public String getBldNm() { return bldNm; }
        public String getSplotNm() { return splotNm; }
        public String getBlock() { return block; }
        public String getLot() { return lot; }
        public String getNaRoadCd() { return naRoadCd; }
        public String getNaBjdongCd() { return naBjdongCd; }
        public String getNaUgrndCd() { return naUgrndCd; }
        public String getNaMainBun() { return naMainBun; }
        public String getNaSubBun() { return naSubBun; }
        public String getDongNm() { return dongNm; }
        public String getHoNm() { return hoNm; }
        public String getFlrGbCd() { return flrGbCd; }
        public String getFlrGbCdNm() { return flrGbCdNm; }
        public String getFlrNo() { return flrNo; }
        public String getFlrNoNm() { return flrNoNm; }
        public String getExposPubuseGbCd() { return exposPubuseGbCd; }
        public String getExposPubuseGbCdNm() { return exposPubuseGbCdNm; }
        public String getMainAtchGbCd() { return mainAtchGbCd; }
        public String getMainAtchGbCdNm() { return mainAtchGbCdNm; }
        public String getStrctCd() { return strctCd; }
        public String getStrctCdNm() { return strctCdNm; }
        public String getEtcStrct() { return etcStrct; }
        public String getMainPurpsCd() { return mainPurpsCd; }
        public String getMainPurpsCdNm() { return mainPurpsCdNm; }
        public String getEtcPurps() { return etcPurps; }
        public String getArea() { return area; }
        public String getCrtnDay() { return crtnDay; }
        public String getRnum() { return rnum; }
        public String getPlatPlc() { return platPlc; }
        public String getSigunguCd() { return sigunguCd; }
        public String getBjdongCd() { return bjdongCd; }
        public String getPlatGbCd() { return platGbCd; }
        public String getBun() { return bun; }
        public String getJi() { return ji; }
        public String getMgmBldrgstPk() { return mgmBldrgstPk; }
    }
}
