package com.example.ganpativargani.mapper;

import com.example.ganpativargani.dto.DonationDto;
import com.example.ganpativargani.entity.Donation;
import org.springframework.stereotype.Component;

@Component
public class DonationMapper {

    private final ContributorMapper contributorMapper;

    public DonationMapper(ContributorMapper contributorMapper) {
        this.contributorMapper = contributorMapper;
    }

    public DonationDto toDto(Donation donation) {
        return DonationDto.builder()
                .id(donation.getId())
                .contributor(contributorMapper.toDto(donation.getContributor()))
                .amount(donation.getAmount())
                .donationDate(donation.getDonationDate())
                .description(donation.getDescription())
                .whatsappStatus(donation.getWhatsappStatus())
                .whatsappMessageId(donation.getWhatsappMessageId())
                .whatsappSentAt(donation.getWhatsappSentAt())
                .whatsappErrorMessage(donation.getWhatsappErrorMessage())
                .createdAt(donation.getCreatedAt())
                .updatedAt(donation.getUpdatedAt())
                .build();
    }
}
