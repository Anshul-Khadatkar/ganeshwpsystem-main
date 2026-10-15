package com.example.ganpativargani.service;

import com.example.ganpativargani.common.PhoneNumberUtil;
import com.example.ganpativargani.dto.DonationCreateRequest;
import com.example.ganpativargani.dto.DonationDto;
import com.example.ganpativargani.dto.DonationUpdateRequest;
import com.example.ganpativargani.dto.PageResponse;
import com.example.ganpativargani.entity.Building;
import com.example.ganpativargani.entity.Contributor;
import com.example.ganpativargani.entity.Donation;
import com.example.ganpativargani.entity.WhatsAppStatus;
import com.example.ganpativargani.exception.BadRequestException;
import com.example.ganpativargani.exception.ResourceNotFoundException;
import com.example.ganpativargani.mapper.DonationMapper;
import com.example.ganpativargani.repository.BuildingRepository;
import com.example.ganpativargani.repository.ContributorRepository;
import com.example.ganpativargani.repository.DonationRepository;
import com.example.ganpativargani.specification.DonationSpecification;
import com.example.ganpativargani.specification.DonationSpecification.DonationFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class DonationService {

    /**
     * Whitelisted sort keys mapped to safe JPA property paths. Using an
     * explicit whitelist (rather than trusting a raw "sort=" query param)
     * prevents arbitrary/invalid property-path injection.
     */
    private static final Map<String, String> SORT_FIELD_MAP = Map.of(
            "amount", "amount",
            "donationDate", "donationDate",
            "name", "contributor.name",
            "building", "contributor.building.name",
            "floor", "contributor.floorNumber",
            "flat", "contributor.flatNumber",
            "createdAt", "createdAt"
    );

    private static final Set<String> VALID_SORT_DIRECTIONS = Set.of("asc", "desc");

    private final DonationRepository donationRepository;
    private final ContributorRepository contributorRepository;
    private final BuildingRepository buildingRepository;
    private final DonationMapper donationMapper;
    private final AuditService auditService;
    private final WhatsAppService whatsAppService;

    public DonationService(DonationRepository donationRepository,
                            ContributorRepository contributorRepository,
                            BuildingRepository buildingRepository,
                            DonationMapper donationMapper,
                            AuditService auditService,
                            WhatsAppService whatsAppService) {
        this.donationRepository = donationRepository;
        this.contributorRepository = contributorRepository;
        this.buildingRepository = buildingRepository;
        this.donationMapper = donationMapper;
        this.auditService = auditService;
        this.whatsAppService = whatsAppService;
    }

    @Transactional
    public DonationDto create(DonationCreateRequest request) {
        Building building = buildingRepository.findByIdAndDeletedFalse(request.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Building not found: " + request.getBuildingId()));

        validateLocationFields(building, request.getFloorNumber(), request.getFlatNumber());

        String normalizedMobile = PhoneNumberUtil.normalize(request.getMobileNumber());
        Contributor contributor = findOrCreateContributor(request, building, normalizedMobile);

        Donation donation = Donation.builder()
                .contributor(contributor)
                .amount(request.getAmount())
                .donationDate(request.getDonationDate())
                .description(request.getDescription())
                .whatsappStatus(WhatsAppStatus.NOT_SENT)
                .build();

        donation = donationRepository.save(donation);
        auditService.log("Donation", donation.getId(), "CREATE",
                "Recorded donation of " + donation.getAmount() + " for " + contributor.getName());

        if (request.isSendWhatsAppThankYou()) {
            // Best-effort: a WhatsApp failure (e.g. no default template configured yet,
            // or the provider rejecting the send) must never fail the donation save
            // itself - the donation is already committed above. Any failure just leaves
            // donation.whatsappStatus = FAILED for the admin to retry from the Donations page.
            try {
                var sendRequest = new com.example.ganpativargani.dto.whatsapp.DonationSendRequest();
                sendRequest.setTemplateId(request.getThankYouTemplateId());
                return whatsAppService.sendDonationThankYou(donation.getId(), sendRequest);
            } catch (Exception ex) {
                log.warn("WhatsApp thank-you send failed for donation {}", donation.getId(), ex);
            }
        }

        // No WhatsApp send requested - donation stays whatsappStatus = NOT_SENT,
        // sendable later from the Donations page.
        return donationMapper.toDto(donation);
    }

    @Transactional
    public DonationDto update(Long id, DonationUpdateRequest request) {
        Donation donation = findEntity(id);
        donation.setAmount(request.getAmount());
        donation.setDonationDate(request.getDonationDate());
        donation.setDescription(request.getDescription());
        donation = donationRepository.save(donation);
        auditService.log("Donation", donation.getId(), "UPDATE", "Updated donation");
        return donationMapper.toDto(donation);
    }

    @Transactional
    public void delete(Long id) {
        Donation donation = findEntity(id);
        donation.markDeleted();
        donationRepository.save(donation);
        auditService.log("Donation", donation.getId(), "DELETE", "Soft-deleted donation");
    }

    @Transactional(readOnly = true)
    public DonationDto getById(Long id) {
        return donationMapper.toDto(findEntity(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<DonationDto> search(DonationFilter filter, int page, int size, String sortBy, String sortDir) {
        Sort sort = resolveSort(sortBy, sortDir);
        Page<Donation> result = donationRepository.findAll(
                DonationSpecification.build(filter),
                PageRequest.of(page, size, sort)
        );
        return PageResponse.from(result.map(donationMapper::toDto));
    }

    // ---- helpers ----

    private Donation findEntity(Long id) {
        return donationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found: " + id));
    }

    private void validateLocationFields(Building building, String floorNumber, String flatNumber) {
        if (building.isRequiresFloor() && !StringUtils.hasText(floorNumber)) {
            throw new BadRequestException("Floor number is required for " + building.getName());
        }
        if (building.isRequiresFlat() && !StringUtils.hasText(flatNumber)) {
            throw new BadRequestException("Flat number is required for " + building.getName());
        }
    }

    private Contributor findOrCreateContributor(DonationCreateRequest request, Building building, String normalizedMobile) {
        String flat = building.isRequiresFlat() ? request.getFlatNumber() : null;

        return contributorRepository
                .findByMobileNumberAndBuilding_IdAndFlatNumberAndDeletedFalse(normalizedMobile, building.getId(), flat)
                .map(existing -> {
                    // Keep the contributor's display name/floor current with the latest donation entry.
                    existing.setName(request.getName().trim());
                    existing.setFloorNumber(building.isRequiresFloor() ? request.getFloorNumber() : null);
                    return existing;
                })
                .orElseGet(() -> contributorRepository.save(Contributor.builder()
                        .name(request.getName().trim())
                        .mobileNumber(normalizedMobile)
                        .building(building)
                        .floorNumber(building.isRequiresFloor() ? request.getFloorNumber() : null)
                        .flatNumber(flat)
                        .whatsappOptIn(true)
                        .build()));
    }

    private Sort resolveSort(String sortBy, String sortDir) {
        String property = SORT_FIELD_MAP.getOrDefault(sortBy, "createdAt");
        String direction = VALID_SORT_DIRECTIONS.contains(sortDir == null ? "" : sortDir.toLowerCase())
                ? sortDir.toLowerCase()
                : "desc";
        return direction.equals("asc") ? Sort.by(property).ascending() : Sort.by(property).descending();
    }

    /** Exposed so the controller can build a filter from raw request params without duplicating logic elsewhere. */
    public static DonationFilter buildFilter(Long buildingId, BigDecimal minAmount, BigDecimal maxAmount,
                                              String floor, String flat, java.time.LocalDate dateFrom,
                                              java.time.LocalDate dateTo, String search, WhatsAppStatus whatsappStatus) {
        return new DonationFilter(buildingId, minAmount, maxAmount, floor, flat, dateFrom, dateTo, search, whatsappStatus);
    }
}
