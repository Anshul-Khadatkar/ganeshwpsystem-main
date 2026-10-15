package com.example.ganpativargani.specification;

import com.example.ganpativargani.entity.Contributor;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class ContributorSpecification {

    private ContributorSpecification() {
    }

    public static Specification<Contributor> build(String search, Long buildingId, Boolean whatsappOptInOnly) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if (buildingId != null) {
                predicates.add(cb.equal(root.get("building").get("id"), buildingId));
            }
            if (Boolean.TRUE.equals(whatsappOptInOnly)) {
                predicates.add(cb.isTrue(root.get("whatsappOptIn")));
            }
            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("mobileNumber")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
