package com.example.ganpativargani.controller;

import com.example.ganpativargani.dto.DonationDto;
import com.example.ganpativargani.dto.analytics.BuildingAnalyticsDto;
import com.example.ganpativargani.dto.analytics.FlatAnalyticsDto;
import com.example.ganpativargani.dto.analytics.FloorAnalyticsDto;
import com.example.ganpativargani.dto.analytics.SummaryStatsDto;
import com.example.ganpativargani.dto.analytics.ThresholdBucketDto;
import com.example.ganpativargani.dto.analytics.TopDonorDto;
import com.example.ganpativargani.dto.analytics.WhatsAppStatsDto;
import com.example.ganpativargani.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /** buildingId omitted or null => overall "All Buildings" statistics. */
    @GetMapping("/summary")
    public SummaryStatsDto summary(@RequestParam(required = false) Long buildingId) {
        return analyticsService.getSummary(buildingId);
    }

    @GetMapping("/buildings")
    public List<BuildingAnalyticsDto> buildingWise() {
        return analyticsService.getBuildingWise();
    }

    @GetMapping("/buildings/{buildingId}")
    public BuildingAnalyticsDto buildingDetail(@PathVariable Long buildingId) {
        return analyticsService.getBuildingDetail(buildingId);
    }

    @GetMapping("/buildings/{buildingId}/floors")
    public List<FloorAnalyticsDto> floorWise(@PathVariable Long buildingId) {
        return analyticsService.getFloorWise(buildingId);
    }

    @GetMapping("/buildings/{buildingId}/flats")
    public List<FlatAnalyticsDto> flatWise(@PathVariable Long buildingId) {
        return analyticsService.getFlatWise(buildingId);
    }

    @GetMapping("/top-donors")
    public List<TopDonorDto> topDonors(@RequestParam(required = false) Long buildingId,
                                        @RequestParam(defaultValue = "10") int limit) {
        return analyticsService.getTopDonors(buildingId, limit);
    }

    @GetMapping("/recent-donations")
    public List<DonationDto> recentDonations(@RequestParam(required = false) Long buildingId,
                                              @RequestParam(defaultValue = "10") int limit) {
        return analyticsService.getRecentDonations(buildingId, limit);
    }

    @GetMapping("/thresholds")
    public List<ThresholdBucketDto> thresholds(@RequestParam(required = false) Long buildingId) {
        return analyticsService.getThresholds(buildingId);
    }

    @GetMapping("/whatsapp-stats")
    public WhatsAppStatsDto whatsappStats(@RequestParam(required = false) Long buildingId) {
        return analyticsService.getWhatsAppStats(buildingId);
    }
}
