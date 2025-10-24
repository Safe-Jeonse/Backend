package com.safe_jeonse.server.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record JusoApiResponse(
        JusoResult result,
        int errCd,
        String errMsg,
        String id,
        String trId
) {

    public record JusoResult(
            String returncount,
            String totalcount,
            List<JusoResultData> resultdata,
            String pagenum,
            String matching
    ) {}

    public record JusoResultData(
            @JsonProperty("addr_type")
            String addrType,

            @JsonProperty("road_nm")
            String roadNm,

            @JsonProperty("adm_cd")
            String admCd,

            @JsonProperty("road_nm_sub_no")
            String roadNmSubNo,

            @JsonProperty("adm_nm")
            String admNm,

            @JsonProperty("jibun_sub_no")
            String jibunSubNo,

            @JsonProperty("bd_sub_nm")
            String bdSubNm,

            @JsonProperty("ri_nm")
            String riNm,

            @JsonProperty("sido_cd")
            String sidoCd,

            @JsonProperty("sgg_nm")
            String sggNm,

            @JsonProperty("sido_nm")
            String sidoNm,

            @JsonProperty("sgg_cd")
            String sggCd,

            @JsonProperty("road_nm_main_no")
            String roadNmMainNo,

            @JsonProperty("road_cd")
            String roadCd,

            @JsonProperty("leg_cd")
            String legCd,

            @JsonProperty("bd_main_nm")
            String bdMainNm,

            String x,
            String y,

            @JsonProperty("leg_nm")
            String legNm,

            @JsonProperty("ri_cd")
            String riCd,

            @JsonProperty("jibun_main_no")
            String jibunMainNo
    ) {}
}
