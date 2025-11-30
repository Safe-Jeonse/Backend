package com.safe_jeonse.server.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "house_public_price_2025",
        indexes = {
                @Index(name = "idx_search", columnList = "bjdongCode, bonbun, bubun, hoName")
        }
)
public class publicPrice {

    @Id
    @Column(name = "unique_no", length = 30)
    private String uniqueNo;

    @Column(name = "std_year", nullable = false)
    private Integer stdYear;

    @Column(name = "std_month", nullable = false)
    private Integer stdMonth;

    @Column(name = "bjdong_code", length = 10, nullable = false)
    private String bjdongCode;

    @Column(name = "road_addr")
    private String roadAddr;

    @Column(name = "sido", length = 50)
    private String sido;

    @Column(name = "sigungu", length = 50)
    private String sigungu;

    @Column(name = "eupmyun", length = 50)
    private String eupmyun;

    @Column(name = "dongri", length = 50)
    private String dongri;

    @Column(name = "bonbun", nullable = false)
    private Integer bonbun;

    @Column(name = "bubun", nullable = false)
    private Integer bubun;

    @Column(name = "apt_name", length = 100)
    private String aptName;

    @Column(name = "dong_name", length = 50)
    private String dongName;

    @Column(name = "ho_name", length = 50)
    private String hoName;

    @Column(name = "area")
    private Double area;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "special_land_code")
    private Integer specialLandCode;

    @Column(name = "special_land_name", length = 100)
    private String specialLandName;

    @Column(name = "apt_code")
    private Integer aptCode;

    @Column(name = "dong_code")
    private Integer dongCode;

    @Column(name = "ho_code")
    private Integer hoCode;

}
