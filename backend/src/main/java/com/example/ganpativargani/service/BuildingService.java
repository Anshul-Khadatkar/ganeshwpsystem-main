package com.example.ganpativargani.service;

import com.example.ganpativargani.dto.BuildingDto;
import com.example.ganpativargani.dto.BuildingRequest;
import com.example.ganpativargani.entity.Building;
import com.example.ganpativargani.exception.BadRequestException;
import com.example.ganpativargani.exception.ResourceNotFoundException;
import com.example.ganpativargani.mapper.BuildingMapper;
import com.example.ganpativargani.repository.BuildingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final BuildingMapper buildingMapper;
    private final AuditService auditService;

    public BuildingService(BuildingRepository buildingRepository, BuildingMapper buildingMapper, AuditService auditService) {
        this.buildingRepository = buildingRepository;
        this.buildingMapper = buildingMapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<BuildingDto> listAll(boolean activeOnly) {
        List<Building> buildings = activeOnly
                ? buildingRepository.findByDeletedFalseAndActiveTrueOrderBySortOrderAscNameAsc()
                : buildingRepository.findByDeletedFalseOrderBySortOrderAscNameAsc();
        return buildings.stream().map(buildingMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public BuildingDto getById(Long id) {
        return buildingMapper.toDto(findEntity(id));
    }

    @Transactional
    public BuildingDto create(BuildingRequest request) {
        if (buildingRepository.existsByNameIgnoreCaseAndDeletedFalse(request.getName())) {
            throw new BadRequestException("A building/category named '" + request.getName() + "' already exists");
        }
        Building building = Building.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .active(request.isActive())
                .requiresFloor(request.isRequiresFloor())
                .requiresFlat(request.isRequiresFlat())
                .sortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder())
                .build();
        building = buildingRepository.save(building);
        auditService.log("Building", building.getId(), "CREATE", "Created building '" + building.getName() + "'");
        return buildingMapper.toDto(building);
    }

    @Transactional
    public BuildingDto update(Long id, BuildingRequest request) {
        Building building = findEntity(id);

        if (!building.getName().equalsIgnoreCase(request.getName())
                && buildingRepository.existsByNameIgnoreCaseAndDeletedFalse(request.getName())) {
            throw new BadRequestException("A building/category named '" + request.getName() + "' already exists");
        }

        building.setName(request.getName().trim());
        building.setDescription(request.getDescription());
        building.setActive(request.isActive());
        building.setRequiresFloor(request.isRequiresFloor());
        building.setRequiresFlat(request.isRequiresFlat());
        building.setSortOrder(request.getSortOrder() == null ? building.getSortOrder() : request.getSortOrder());

        building = buildingRepository.save(building);
        auditService.log("Building", building.getId(), "UPDATE", "Updated building '" + building.getName() + "'");
        return buildingMapper.toDto(building);
    }

    @Transactional
    public BuildingDto setActive(Long id, boolean active) {
        Building building = findEntity(id);
        building.setActive(active);
        building = buildingRepository.save(building);
        auditService.log("Building", building.getId(), active ? "ACTIVATE" : "DEACTIVATE",
                "Set active=" + active + " for building '" + building.getName() + "'");
        return buildingMapper.toDto(building);
    }

    private Building findEntity(Long id) {
        return buildingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found: " + id));
    }
}
