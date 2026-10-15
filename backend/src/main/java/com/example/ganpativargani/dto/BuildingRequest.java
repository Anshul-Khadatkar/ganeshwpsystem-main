package com.example.ganpativargani.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BuildingRequest {

    @NotBlank(message = "Building name is required")
    @Size(max = 100, message = "Building name must be at most 100 characters")
    private String name;

    /** Always optional - never validated as required. */
    private String description;

    private boolean active = true;

    private boolean requiresFloor = true;

    private boolean requiresFlat = true;

    private Integer sortOrder = 0;
}
