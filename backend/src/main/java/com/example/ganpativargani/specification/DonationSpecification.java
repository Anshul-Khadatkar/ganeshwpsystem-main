package com.example.ganpativargani.specification;

import com.example.ganpativargani.entity.Building;
import com.example.ganpativargani.entity.Contributor;
import com.example.ganpativargani.entity.Donation;
import com.example.ganpativargani.entity.WhatsAppStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a single combinable {@link Specification} from whatever filter
 * values are present. This is the one place donation filtering logic
 * lives - the Donations list page, and later the WhatsApp broadcast
 * recipient selector, both reuse this instead of duplicating queries.
 */
public final class DonationSpecification {

    private DonationSpecification() {
    }

    public static Specification<Donation> build(DonationFilter filter) {
        return (root, query, cb) -> {

            // Avoid N+1 selects when fetching the actual page of results
            // (not needed, and not allowed by JPA, on the COUNT query).
            if (!Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                var contributorFetch = root.fetch("contributor", JoinType.LEFT);
                contributorFetch.fetch("building", JoinType.LEFT);
                query.distinct(true);
            }

            Join<Donation, Contributor> contributor = root.join("contributor", JoinType.LEFT);
            Join<Contributor, Building> building = contributor.join("building", JoinType.LEFT);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if (filter.buildingId() != null) {
                predicates.add(cb.equal(building.get("id"), filter.buildingId()));
            }

            if (filter.minAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), filter.minAmount()));
            }

            if (filter.maxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), filter.maxAmount()));
            }

            if (filter.floor() != null && !filter.floor().isBlank()) {
                predicates.add(cb.equal(contributor.get("floorNumber"), filter.floor()));
            }

            if (filter.flat() != null && !filter.flat().isBlank()) {
                predicates.add(cb.equal(contributor.get("flatNumber"), filter.flat()));
            }

            if (filter.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("donationDate"), filter.dateFrom()));
            }

            if (filter.dateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("donationDate"), filter.dateTo()));
            }

            if (filter.whatsappStatus() != null) {
                predicates.add(cb.equal(root.get("whatsappStatus"), filter.whatsappStatus()));
            }

            if (filter.search() != null && !filter.search().isBlank()) {
                String like = "%" + filter.search().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(contributor.get("name")), like),
                        cb.like(cb.lower(contributor.get("mobileNumber")), like),
                        cb.like(cb.lower(contributor.get("flatNumber")), like)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /** Immutable holder for whatever filter combination the caller supplied. */
    public record DonationFilter(
            Long buildingId,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String floor,
            String flat,
            LocalDate dateFrom,
            LocalDate dateTo,
            String search,
            WhatsAppStatus whatsappStatus
    ) {
    }
}
