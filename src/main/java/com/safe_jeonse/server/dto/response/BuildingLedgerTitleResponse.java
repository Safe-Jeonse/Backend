package com.safe_jeonse.server.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.List;

/*
   건축물대장 표제부 응답
 */
public record BuildingLedgerTitleResponse(
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
            @JsonProperty("mainPurpsCdNm") String mainPurpsCdNm,
            @JsonProperty("etcPurps") String etcPurps,
            @JsonProperty("roofCd") String roofCd,
            @JsonProperty("roofCdNm") String roofCdNm,
            @JsonProperty("etcRoof") String etcRoof,
            @JsonProperty("hhldCnt") String hhldCnt,
            @JsonProperty("fmlyCnt") String fmlyCnt,
            @JsonProperty("heit") String heit,
            @JsonProperty("grndFlrCnt") String grndFlrCnt,
            @JsonProperty("ugrndFlrCnt") String ugrndFlrCnt,
            @JsonProperty("rideUseElvtCnt") String rideUseElvtCnt,
            @JsonProperty("emgenUseElvtCnt") String emgenUseElvtCnt,
            @JsonProperty("atchBldCnt") String atchBldCnt,
            @JsonProperty("atchBldArea") String atchBldArea,
            @JsonProperty("totDongTotArea") String totDongTotArea,
            @JsonProperty("indrMechUtcnt") String indrMechUtcnt,
            @JsonProperty("indrMechArea") String indrMechArea,
            @JsonProperty("oudrMechUtcnt") String oudrMechUtcnt,
            @JsonProperty("oudrMechArea") String oudrMechArea,
            @JsonProperty("indrAutoUtcnt") String indrAutoUtcnt,
            @JsonProperty("indrAutoArea") String indrAutoArea,
            @JsonProperty("oudrAutoUtcnt") String oudrAutoUtcnt,
            @JsonProperty("oudrAutoArea") String oudrAutoArea,
            @JsonProperty("pmsDay") String pmsDay,
            @JsonProperty("stcnsDay") String stcnsDay,
            @JsonProperty("useAprDay") String useAprDay,
            @JsonProperty("pmsnoYear") String pmsnoYear,
            @JsonProperty("pmsnoKikCd") String pmsnoKikCd,
            @JsonProperty("pmsnoKikCdNm") String pmsnoKikCdNm,
            @JsonProperty("pmsnoGbCd") String pmsnoGbCd,
            @JsonProperty("pmsnoGbCdNm") String pmsnoGbCdNm,
            @JsonProperty("hoCnt") String hoCnt,
            @JsonProperty("engrGrade") String engrGrade,
            @JsonProperty("engrRat") String engrRat,
            @JsonProperty("engrEpi") String engrEpi,
            @JsonProperty("gnBldGrade") String gnBldGrade,
            @JsonProperty("gnBldCert") String gnBldCert,
            @JsonProperty("itgBldGrade") String itgBldGrade,
            @JsonProperty("itgBldCert") String itgBldCert,
            @JsonProperty("crtnDay") String crtnDay,
            @JsonProperty("rnum") String rnum,
            @JsonProperty("platPlc") String platPlc,
            @JsonProperty("sigunguCd") String sigunguCd,
            @JsonProperty("bjdongCd") String bjdongCd,
            @JsonProperty("platGbCd") String platGbCd,
            @JsonProperty("bun") String bun,
            @JsonProperty("ji") String ji,
            @JsonProperty("mgmBldrgstPk") String mgmBldrgstPk,
            @JsonProperty("regstrGbCd") String regstrGbCd,
            @JsonProperty("regstrGbCdNm") String regstrGbCdNm,
            @JsonProperty("regstrKindCd") String regstrKindCd,
            @JsonProperty("regstrKindCdNm") String regstrKindCdNm,
            @JsonProperty("newPlatPlc") String newPlatPlc,
            @JsonProperty("bldNm") String bldNm,
            @JsonProperty("splotNm") String splotNm,
            @JsonProperty("block") String block,
            @JsonProperty("lot") String lot,
            @JsonProperty("bylotCnt") String bylotCnt,
            @JsonProperty("naRoadCd") String naRoadCd,
            @JsonProperty("naBjdongCd") String naBjdongCd,
            @JsonProperty("naUgrndCd") String naUgrndCd,
            @JsonProperty("naMainBun") String naMainBun,
            @JsonProperty("naSubBun") String naSubBun,
            @JsonProperty("dongNm") String dongNm,
            @JsonProperty("mainAtchGbCd") String mainAtchGbCd,
            @JsonProperty("mainAtchGbCdNm") String mainAtchGbCdNm,
            @JsonProperty("platArea") String platArea,
            @JsonProperty("archArea") String archArea,
            @JsonProperty("bcRat") String bcRat,
            @JsonProperty("totArea") String totArea,
            @JsonProperty("vlRatEstmTotArea") String vlRatEstmTotArea,
            @JsonProperty("vlRat") String vlRat,
            @JsonProperty("strctCd") String strctCd,
            @JsonProperty("strctCdNm") String strctCdNm,
            @JsonProperty("etcStrct") String etcStrct,
            @JsonProperty("mainPurpsCd") String mainPurpsCd,
            @JsonProperty("rserthqkDsgnApplyYn") String rserthqkDsgnApplyYn,
            @JsonProperty("rserthqkAblty") String rserthqkAblty
    ) {
        public String getMainPurpsCdNm() { return mainPurpsCdNm; }
        public String getEtcPurps() { return etcPurps; }
        public String getRoofCd() { return roofCd; }
        public String getRoofCdNm() { return roofCdNm; }
        public String getEtcRoof() { return etcRoof; }
        public String getHhldCnt() { return hhldCnt; }
        public String getFmlyCnt() { return fmlyCnt; }
        public String getHeit() { return heit; }
        public String getGrndFlrCnt() { return grndFlrCnt; }
        public String getUgrndFlrCnt() { return ugrndFlrCnt; }
        public String getRideUseElvtCnt() { return rideUseElvtCnt; }
        public String getEmgenUseElvtCnt() { return emgenUseElvtCnt; }
        public String getAtchBldCnt() { return atchBldCnt; }
        public String getAtchBldArea() { return atchBldArea; }
        public String getTotDongTotArea() { return totDongTotArea; }
        public String getIndrMechUtcnt() { return indrMechUtcnt; }
        public String getIndrMechArea() { return indrMechArea; }
        public String getOudrMechUtcnt() { return oudrMechUtcnt; }
        public String getOudrMechArea() { return oudrMechArea; }
        public String getIndrAutoUtcnt() { return indrAutoUtcnt; }
        public String getIndrAutoArea() { return indrAutoArea; }
        public String getOudrAutoUtcnt() { return oudrAutoUtcnt; }
        public String getOudrAutoArea() { return oudrAutoArea; }
        public String getPmsDay() { return pmsDay; }
        public String getStcnsDay() { return stcnsDay; }
        public String getUseAprDay() { return useAprDay; }
        public String getPmsnoYear() { return pmsnoYear; }
        public String getPmsnoKikCd() { return pmsnoKikCd; }
        public String getPmsnoKikCdNm() { return pmsnoKikCdNm; }
        public String getPmsnoGbCd() { return pmsnoGbCd; }
        public String getPmsnoGbCdNm() { return pmsnoGbCdNm; }
        public String getHoCnt() { return hoCnt; }
        public String getEngrGrade() { return engrGrade; }
        public String getEngrRat() { return engrRat; }
        public String getEngrEpi() { return engrEpi; }
        public String getGnBldGrade() { return gnBldGrade; }
        public String getGnBldCert() { return gnBldCert; }
        public String getItgBldGrade() { return itgBldGrade; }
        public String getItgBldCert() { return itgBldCert; }
        public String getCrtnDay() { return crtnDay; }
        public String getRnum() { return rnum; }
        public String getPlatPlc() { return platPlc; }
        public String getSigunguCd() { return sigunguCd; }
        public String getBjdongCd() { return bjdongCd; }
        public String getPlatGbCd() { return platGbCd; }
        public String getBun() { return bun; }
        public String getJi() { return ji; }
        public String getMgmBldrgstPk() { return mgmBldrgstPk; }
        public String getRegstrGbCd() { return regstrGbCd; }
        public String getRegstrGbCdNm() { return regstrGbCdNm; }
        public String getRegstrKindCd() { return regstrKindCd; }
        public String getRegstrKindCdNm() { return regstrKindCdNm; }
        public String getNewPlatPlc() { return newPlatPlc; }
        public String getBldNm() { return bldNm; }
        public String getSplotNm() { return splotNm; }
        public String getBlock() { return block; }
        public String getLot() { return lot; }
        public String getBylotCnt() { return bylotCnt; }
        public String getNaRoadCd() { return naRoadCd; }
        public String getNaBjdongCd() { return naBjdongCd; }
        public String getNaUgrndCd() { return naUgrndCd; }
        public String getNaMainBun() { return naMainBun; }
        public String getNaSubBun() { return naSubBun; }
        public String getDongNm() { return dongNm; }
        public String getMainAtchGbCd() { return mainAtchGbCd; }
        public String getMainAtchGbCdNm() { return mainAtchGbCdNm; }
        public String getPlatArea() { return platArea; }
        public String getArchArea() { return archArea; }
        public String getBcRat() { return bcRat; }
        public String getTotArea() { return totArea; }
        public String getVlRatEstmTotArea() { return vlRatEstmTotArea; }
        public String getVlRat() { return vlRat; }
        public String getStrctCd() { return strctCd; }
        public String getStrctCdNm() { return strctCdNm; }
        public String getEtcStrct() { return etcStrct; }
        public String getMainPurpsCd() { return mainPurpsCd; }
        public String getRserthqkDsgnApplyYn() { return rserthqkDsgnApplyYn; }
        public String getRserthqkAblty() { return rserthqkAblty; }
    }
}
