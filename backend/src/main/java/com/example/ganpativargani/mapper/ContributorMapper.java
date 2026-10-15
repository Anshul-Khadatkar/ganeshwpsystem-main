package com.example.ganpativargani.mapper;

import com.example.ganpativargani.dto.ContributorDto;
import com.example.ganpativargani.entity.Contributor;
import org.springframework.stereotype.Component;

@Component
public class ContributorMapper {

    public ContributorDto toDto(Contributor contributor) {
        return ContributorDto.builder()
                .id(contributor.getId())
                .name(contributor.getName())
                .mobileNumber(contributor.getMobileNumber())
                .buildingId(contributor.getBuilding().getId())
                .buildingName(contributor.getBuilding().getName())
                .floorNumber(contributor.getFloorNumber())
                .flatNumber(contributor.getFlatNumber())
                .whatsappOptIn(contributor.isWhatsappOptIn())
                .build();
    }
}
