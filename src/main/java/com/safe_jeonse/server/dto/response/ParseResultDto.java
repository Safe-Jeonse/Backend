package com.safe_jeonse.server.dto.response;

import java.time.LocalDate;
import java.util.List;

public record ParseResultDto(
        String address,
        String uniqueNumber,
        CoverSection coverSection,
        GapguSection gapguSection,
        EulguSection eulguSection
) {
    public record CoverSection(
            BuildingInfo mainBuildingInfo,
            List<LandInfo> landInfo,
            PropertyPartInfo propertyInfo,
            List<LandRight> landRights
    ) {}

    public record GapguSection(
            List<OwnershipHistory> ownershipHistory
    ) {}
    public record EulguSection(
            List<LienHistory> lienHistory
    ) {}

    public record BuildingInfo(String address, String buildingName, String structure, int totalFloors) {}
    public record LandInfo(int sequence, String address, String category, double area) {}
    public record PropertyPartInfo(String floorAndUnit, String structure, double area) {}
    public record LandRight(String type, String ratio) {}
    public record OwnershipHistory(
            int sequence,
            LocalDate receiptDate,
            String cause,
            String ownerName,
            Long dealPrice
    ) {}

    public record LienHistory(
            int sequence,
            String type,
            LocalDate receiptDate,
            String cause,
            String debtor,
            String creditor,
            long amount,
            boolean isTerminated
    ) {}
}

