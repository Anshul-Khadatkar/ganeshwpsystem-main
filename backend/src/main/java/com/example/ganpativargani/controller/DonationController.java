package com.example.ganpativargani.controller;

import com.example.ganpativargani.dto.DonationCreateRequest;
import com.example.ganpativargani.dto.DonationDto;
import com.example.ganpativargani.dto.DonationUpdateRequest;
import com.example.ganpativargani.dto.PageResponse;
import com.example.ganpativargani.entity.WhatsAppStatus;
import com.example.ganpativargani.service.DonationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DonationDto create(@Valid @RequestBody DonationCreateRequest request) {
        return donationService.create(request);
    }

    @GetMapping("/{id}")
    public DonationDto getById(@PathVariable Long id) {
        return donationService.getById(id);
    }

    @PutMapping("/{id}")
    public DonationDto update(@PathVariable Long id, @Valid @RequestBody DonationUpdateRequest request) {
        return donationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        donationService.delete(id);
    }

    /**
     * All filters below are combinable, e.g.
     * GET /api/donations?buildingId=1&minAmount=2500&floor=3&sortBy=amount&sortDir=desc
     * <p>
     * dateFrom/dateTo are plain ISO dates (yyyy-MM-dd) - the frontend translates
     * presets like "This week" / "This month" into an explicit range before calling this.
     */
    @GetMapping
    public PageResponse<DonationDto> search(
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String floor,
            @RequestParam(required = false) String flat,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) WhatsAppStatus whatsappStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        var filter = DonationService.buildFilter(buildingId, minAmount, maxAmount, floor, flat, dateFrom, dateTo, search, whatsappStatus);
        return donationService.search(filter, page, Math.min(size, 100), sortBy, sortDir);
    }
}
