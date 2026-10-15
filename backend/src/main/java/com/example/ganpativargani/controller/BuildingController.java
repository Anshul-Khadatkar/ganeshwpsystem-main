package com.example.ganpativargani.controller;

import com.example.ganpativargani.dto.BuildingDto;
import com.example.ganpativargani.dto.BuildingRequest;
import com.example.ganpativargani.service.BuildingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;

    @GetMapping
    public List<BuildingDto> list(@RequestParam(defaultValue = "false") boolean activeOnly) {
        return buildingService.listAll(activeOnly);
    }

    @GetMapping("/{id}")
    public BuildingDto getById(@PathVariable Long id) {
        return buildingService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BuildingDto create(@Valid @RequestBody BuildingRequest request) {
        return buildingService.create(request);
    }

    @PutMapping("/{id}")
    public BuildingDto update(@PathVariable Long id, @Valid @RequestBody BuildingRequest request) {
        return buildingService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public BuildingDto setStatus(@PathVariable Long id, @RequestParam boolean active) {
        return buildingService.setActive(id, active);
    }
}
