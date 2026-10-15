package com.example.ganpativargani.repository;

import com.example.ganpativargani.entity.Contributor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ContributorRepository extends JpaRepository<Contributor, Long>, JpaSpecificationExecutor<Contributor> {

    Optional<Contributor> findByIdAndDeletedFalse(Long id);

    Optional<Contributor> findByMobileNumberAndBuilding_IdAndFlatNumberAndDeletedFalse(
            String mobileNumber, Long buildingId, String flatNumber);

    /**
     * Resolves broadcast recipients. Always enforces {@code whatsappOptIn = true}
     * - opt-out contributors are never eligible, regardless of what filters
     * are supplied. All filter params are optional and combinable:
     * null buildingId = any building; null minAmount/dateFrom/dateTo = no
     * donation-based restriction (so "All Contributors" still returns people
     * who haven't donated yet).
     */
    @Query("SELECT DISTINCT c FROM Contributor c " +
           "LEFT JOIN Donation d ON d.contributor = c AND d.deleted = false " +
           "WHERE c.deleted = false AND c.whatsappOptIn = true " +
           "AND (:buildingId IS NULL OR c.building.id = :buildingId) " +
           "AND (:minAmount IS NULL OR d.amount >= :minAmount) " +
           "AND (:dateFrom IS NULL OR d.donationDate >= :dateFrom) " +
           "AND (:dateTo IS NULL OR d.donationDate <= :dateTo)")
    List<Contributor> findBroadcastRecipients(
            @Param("buildingId") Long buildingId,
            @Param("minAmount") java.math.BigDecimal minAmount,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );
}
