package com.example.ganpativargani.mapper;

import com.example.ganpativargani.dto.BuildingDto;
import com.example.ganpativargani.entity.Building;
import org.springframework.stereotype.Component;

@Component
public class BuildingMapper {

    public BuildingDto toDto(Building building) {
        return BuildingDto.builder()
                .id(building.getId())
                .name(building.getName())
                .description(building.getDescription())
                .active(building.isActive())
                .requiresFloor(building.isRequiresFloor())
                .requiresFlat(building.isRequiresFlat())
                .sortOrder(building.getSortOrder())
                .createdAt(building.getCreatedAt())
                .updatedAt(building.getUpdatedAt())
                .build();
    }
}
