package com.example.ganpativargani.service;

import com.example.ganpativargani.dto.ContributorDto;
import com.example.ganpativargani.dto.PageResponse;
import com.example.ganpativargani.mapper.ContributorMapper;
import com.example.ganpativargani.repository.ContributorRepository;
import com.example.ganpativargani.specification.ContributorSpecification;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContributorService {

    private final ContributorRepository contributorRepository;
    private final ContributorMapper contributorMapper;

    public ContributorService(ContributorRepository contributorRepository, ContributorMapper contributorMapper) {
        this.contributorRepository = contributorRepository;
        this.contributorMapper = contributorMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<ContributorDto> search(String search, Long buildingId, Boolean whatsappOptInOnly, int page, int size) {
        var result = contributorRepository.findAll(
                ContributorSpecification.build(search, buildingId, whatsappOptInOnly),
                PageRequest.of(page, size, Sort.by("name").ascending())
        );
        return PageResponse.from(result.map(contributorMapper::toDto));
    }
}
