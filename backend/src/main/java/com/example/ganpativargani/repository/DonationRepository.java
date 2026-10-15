package com.example.ganpativargani.repository;

import com.example.ganpativargani.entity.Donation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DonationRepository extends JpaRepository<Donation, Long>, JpaSpecificationExecutor<Donation> {

    Optional<Donation> findByIdAndDeletedFalse(Long id);

    Optional<Donation> findByWhatsappMessageId(String whatsappMessageId);

    /**
     * Raw summary row: [totalDonation, distinctDonorCount, donationCount, highestDonation, lowestDonation].
     * Average is deliberately NOT computed here - JPQL AVG() returns Double, which is unsafe
     * for money. The service layer derives the average from totalDonation / donationCount
     * using BigDecimal so rounding stays exact.
     */
    @Query("SELECT COALESCE(SUM(d.amount), 0), COUNT(DISTINCT d.contributor.id), COUNT(d), " +
           "COALESCE(MAX(d.amount), 0), COALESCE(MIN(d.amount), 0) " +
           "FROM Donation d WHERE d.deleted = false " +
           "AND (:buildingId IS NULL OR d.contributor.building.id = :buildingId)")
    List<Object[]> findSummaryRaw(@Param("buildingId") Long buildingId);

    /** Raw row per floor: [floorNumber, totalDonation, distinctDonorCount]. */
    @Query("SELECT c.floorNumber, COALESCE(SUM(d.amount), 0), COUNT(DISTINCT c.id) " +
           "FROM Donation d JOIN d.contributor c " +
           "WHERE d.deleted = false AND c.deleted = false AND c.building.id = :buildingId " +
           "GROUP BY c.floorNumber ORDER BY c.floorNumber")
    List<Object[]> findFloorWiseRaw(@Param("buildingId") Long buildingId);

    /** Raw row per contributor with a location: [contributorId, name, floorNumber, flatNumber, totalDonation, donationCount]. */
    @Query("SELECT c.id, c.name, c.floorNumber, c.flatNumber, COALESCE(SUM(d.amount), 0), COUNT(d) " +
           "FROM Donation d JOIN d.contributor c " +
           "WHERE d.deleted = false AND c.deleted = false AND c.building.id = :buildingId " +
           "GROUP BY c.id, c.name, c.floorNumber, c.flatNumber " +
           "ORDER BY c.floorNumber, c.flatNumber")
    List<Object[]> findFlatWiseRaw(@Param("buildingId") Long buildingId);

    /** Raw row per top donor: [contributorId, name, buildingName, totalDonation, donationCount]. */
    @Query("SELECT c.id, c.name, b.name, COALESCE(SUM(d.amount), 0), COUNT(d) " +
           "FROM Donation d JOIN d.contributor c JOIN c.building b " +
           "WHERE d.deleted = false AND c.deleted = false " +
           "AND (:buildingId IS NULL OR b.id = :buildingId) " +
           "GROUP BY c.id, c.name, b.name " +
           "ORDER BY SUM(d.amount) DESC")
    List<Object[]> findTopDonorsRaw(@Param("buildingId") Long buildingId, Pageable pageable);

    /**
     * Raw donation-count buckets by amount range, in a single pass:
     * [under500, r500to999, r1000to2499, r2500to4999, r5000to9999, r10000plus].
     */
    @Query("SELECT " +
           "COALESCE(SUM(CASE WHEN d.amount < 500 THEN 1 ELSE 0 END), 0), " +
           "COALESCE(SUM(CASE WHEN d.amount >= 500 AND d.amount < 1000 THEN 1 ELSE 0 END), 0), " +
           "COALESCE(SUM(CASE WHEN d.amount >= 1000 AND d.amount < 2500 THEN 1 ELSE 0 END), 0), " +
           "COALESCE(SUM(CASE WHEN d.amount >= 2500 AND d.amount < 5000 THEN 1 ELSE 0 END), 0), " +
           "COALESCE(SUM(CASE WHEN d.amount >= 5000 AND d.amount < 10000 THEN 1 ELSE 0 END), 0), " +
           "COALESCE(SUM(CASE WHEN d.amount >= 10000 THEN 1 ELSE 0 END), 0) " +
           "FROM Donation d WHERE d.deleted = false " +
           "AND (:buildingId IS NULL OR d.contributor.building.id = :buildingId)")
    List<Object[]> findThresholdCountsRaw(@Param("buildingId") Long buildingId);

    /** Raw row per WhatsApp status: [status, count]. Statuses with zero donations simply don't appear. */
    @Query("SELECT d.whatsappStatus, COUNT(d) FROM Donation d WHERE d.deleted = false " +
           "AND (:buildingId IS NULL OR d.contributor.building.id = :buildingId) " +
           "GROUP BY d.whatsappStatus")
    List<Object[]> findWhatsAppStatsRaw(@Param("buildingId") Long buildingId);
}
