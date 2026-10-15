package com.example.ganpativargani.service;

import com.example.ganpativargani.dto.DonationDto;
import com.example.ganpativargani.dto.analytics.BuildingAnalyticsDto;
import com.example.ganpativargani.dto.analytics.FlatAnalyticsDto;
import com.example.ganpativargani.dto.analytics.FloorAnalyticsDto;
import com.example.ganpativargani.dto.analytics.SummaryStatsDto;
import com.example.ganpativargani.dto.analytics.ThresholdBucketDto;
import com.example.ganpativargani.dto.analytics.TopDonorDto;
import com.example.ganpativargani.dto.analytics.WhatsAppStatsDto;
import com.example.ganpativargani.entity.Building;
import com.example.ganpativargani.entity.WhatsAppStatus;
import com.example.ganpativargani.exception.ResourceNotFoundException;
import com.example.ganpativargani.mapper.DonationMapper;
import com.example.ganpativargani.repository.BuildingRepository;
import com.example.ganpativargani.repository.DonationRepository;
import com.example.ganpativargani.specification.DonationSpecification;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class AnalyticsService {

    private final DonationRepository donationRepository;
    private final BuildingRepository buildingRepository;
    private final DonationMapper donationMapper;

    public AnalyticsService(DonationRepository donationRepository, BuildingRepository buildingRepository,
                             DonationMapper donationMapper) {
        this.donationRepository = donationRepository;
        this.buildingRepository = buildingRepository;
        this.donationMapper = donationMapper;
    }

    @Transactional(readOnly = true)
    public SummaryStatsDto getSummary(Long buildingId) {
        Object[] row = donationRepository.findSummaryRaw(buildingId).get(0);
        BigDecimal total = (BigDecimal) row[0];
        long donors = (long) row[1];
        long donations = (long) row[2];
        BigDecimal highest = (BigDecimal) row[3];
        BigDecimal lowest = (BigDecimal) row[4];

        return SummaryStatsDto.builder()
                .totalDonation(total)
                .totalDonors(donors)
                .totalDonations(donations)
                .averageDonation(average(total, donations))
                .highestDonation(highest)
                .lowestDonation(lowest)
                .build();
    }

    @Transactional(readOnly = true)
    public List<BuildingAnalyticsDto> getBuildingWise() {
        return buildingRepository.findBuildingWiseRaw().stream()
                .map(row -> {
                    BigDecimal total = (BigDecimal) row[2];
                    long donors = (long) row[3];
                    return BuildingAnalyticsDto.builder()
                            .buildingId((Long) row[0])
                            .buildingName((String) row[1])
                            .totalDonation(total)
                            .totalDonors(donors)
                            .averageDonation(average(total, donors))
                            .highestDonation((BigDecimal) row[4])
                            .lowestDonation((BigDecimal) row[5])
                            .requiresFloor((boolean) row[6])
                            .requiresFlat((boolean) row[7])
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public BuildingAnalyticsDto getBuildingDetail(Long buildingId) {
        Building building = requireBuilding(buildingId);
        SummaryStatsDto summary = getSummary(buildingId);
        return BuildingAnalyticsDto.builder()
                .buildingId(building.getId())
                .buildingName(building.getName())
                .totalDonation(summary.getTotalDonation())
                .totalDonors(summary.getTotalDonors())
                .averageDonation(summary.getAverageDonation())
                .highestDonation(summary.getHighestDonation())
                .lowestDonation(summary.getLowestDonation())
                .requiresFloor(building.isRequiresFloor())
                .requiresFlat(building.isRequiresFlat())
                .build();
    }

    @Transactional(readOnly = true)
    public List<FloorAnalyticsDto> getFloorWise(Long buildingId) {
        Building building = requireBuilding(buildingId);
        if (!building.isRequiresFloor()) {
            return List.of();
        }
        return donationRepository.findFloorWiseRaw(buildingId).stream()
                .map(row -> FloorAnalyticsDto.builder()
                        .floorNumber((String) row[0])
                        .totalDonation((BigDecimal) row[1])
                        .donorCount((long) row[2])
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FlatAnalyticsDto> getFlatWise(Long buildingId) {
        requireBuilding(buildingId);
        return donationRepository.findFlatWiseRaw(buildingId).stream()
                .map(row -> FlatAnalyticsDto.builder()
                        .contributorId((Long) row[0])
                        .contributorName((String) row[1])
                        .floorNumber((String) row[2])
                        .flatNumber((String) row[3])
                        .totalDonation((BigDecimal) row[4])
                        .donationCount((long) row[5])
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TopDonorDto> getTopDonors(Long buildingId, int limit) {
        return donationRepository.findTopDonorsRaw(buildingId, PageRequest.of(0, limit)).stream()
                .map(row -> TopDonorDto.builder()
                        .contributorId((Long) row[0])
                        .contributorName((String) row[1])
                        .buildingName((String) row[2])
                        .totalDonation((BigDecimal) row[3])
                        .donationCount((long) row[4])
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DonationDto> getRecentDonations(Long buildingId, int limit) {
        var filter = DonationService.buildFilter(buildingId, null, null, null, null, null, null, null, null);
        return donationRepository.findAll(
                        DonationSpecification.build(filter),
                        PageRequest.of(0, limit, Sort.by("createdAt").descending()))
                .stream()
                .map(donationMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ThresholdBucketDto> getThresholds(Long buildingId) {
        Object[] row = donationRepository.findThresholdCountsRaw(buildingId).get(0);
        String[] labels = {"Under ₹500", "₹500 – ₹999", "₹1,000 – ₹2,499", "₹2,500 – ₹4,999", "₹5,000 – ₹9,999", "₹10,000+"};
        List<ThresholdBucketDto> buckets = new java.util.ArrayList<>();
        for (int i = 0; i < labels.length; i++) {
            buckets.add(ThresholdBucketDto.builder()
                    .label(labels[i])
                    .donationCount((long) row[i])
                    .build());
        }
        return buckets;
    }

    @Transactional(readOnly = true)
    public WhatsAppStatsDto getWhatsAppStats(Long buildingId) {
        var builder = WhatsAppStatsDto.builder().sent(0).pending(0).failed(0).notSent(0);
        for (Object[] row : donationRepository.findWhatsAppStatsRaw(buildingId)) {
            WhatsAppStatus status = (WhatsAppStatus) row[0];
            long count = (long) row[1];
            switch (status) {
                case SENT -> builder.sent(count);
                case PENDING -> builder.pending(count);
                case FAILED -> builder.failed(count);
                case NOT_SENT -> builder.notSent(count);
            }
        }
        return builder.build();
    }

    private Building requireBuilding(Long buildingId) {
        return buildingRepository.findByIdAndDeletedFalse(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found: " + buildingId));
    }

    /** Computes total/count in BigDecimal, never via JPQL AVG() (which returns Double). */
    private BigDecimal average(BigDecimal total, long count) {
        if (count == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }
}
