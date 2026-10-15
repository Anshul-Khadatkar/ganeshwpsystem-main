package com.example.ganpativargani.controller;

import com.example.ganpativargani.dto.ContributorDto;
import com.example.ganpativargani.dto.PageResponse;
import com.example.ganpativargani.service.ContributorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contributors")
@RequiredArgsConstructor
public class ContributorController {

    private final ContributorService contributorService;

    @GetMapping
    public PageResponse<ContributorDto> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) Boolean whatsappOptInOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return contributorService.search(search, buildingId, whatsappOptInOnly, page, Math.min(size, 100));
    }
}
