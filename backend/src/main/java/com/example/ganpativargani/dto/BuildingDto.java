package com.example.ganpativargani.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class BuildingDto {
    private Long id;
    private String name;
    private String description;
    private boolean active;
    private boolean requiresFloor;
    private boolean requiresFlat;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
