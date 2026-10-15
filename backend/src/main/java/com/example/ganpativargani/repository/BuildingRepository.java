package com.example.ganpativargani.repository;

import com.example.ganpativargani.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BuildingRepository extends JpaRepository<Building, Long>, JpaSpecificationExecutor<Building> {

    List<Building> findByDeletedFalseOrderBySortOrderAscNameAsc();

    List<Building> findByDeletedFalseAndActiveTrueOrderBySortOrderAscNameAsc();

    Optional<Building> findByIdAndDeletedFalse(Long id);

    boolean existsByNameIgnoreCaseAndDeletedFalse(String name);

    /**
     * Raw row per building (LEFT JOINed so buildings with zero donations still appear
     * with zeros, rather than being silently omitted from the analytics list):
     * [buildingId, buildingName, totalDonation, distinctDonorCount, highestDonation,
     *  lowestDonation, requiresFloor, requiresFlat].
     */
    @Query("SELECT b.id, b.name, COALESCE(SUM(d.amount), 0), " +
           "COUNT(DISTINCT CASE WHEN d.id IS NOT NULL THEN c.id END), " +
           "COALESCE(MAX(d.amount), 0), COALESCE(MIN(d.amount), 0), b.requiresFloor, b.requiresFlat " +
           "FROM Building b " +
           "LEFT JOIN Contributor c ON c.building = b AND c.deleted = false " +
           "LEFT JOIN Donation d ON d.contributor = c AND d.deleted = false " +
           "WHERE b.deleted = false " +
           "GROUP BY b.id, b.name, b.requiresFloor, b.requiresFlat, b.sortOrder " +
           "ORDER BY b.sortOrder")
    List<Object[]> findBuildingWiseRaw();
}
