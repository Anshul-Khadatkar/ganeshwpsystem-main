package com.example.ganpativargani.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ContributorDto {
    private Long id;
    private String name;
    private String mobileNumber;
    private Long buildingId;
    private String buildingName;

    /** Null when not applicable for this building category - frontend should render "—". */
    private String floorNumber;

    /** Null when not applicable for this building category - frontend should render "—". */
    private String flatNumber;

    private boolean whatsappOptIn;
}
