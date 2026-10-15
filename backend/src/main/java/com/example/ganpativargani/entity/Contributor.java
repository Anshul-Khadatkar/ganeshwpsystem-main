package com.example.ganpativargani.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A resident / shop-owner / donor. Floor and flat are intentionally
 * nullable Strings (not int) because values like "Ground" or "G-12" are
 * valid, and because Row Houses / Shops legitimately have neither.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
@Entity
@Table(
    name = "contributors",
    indexes = {
        @Index(name = "idx_contributor_mobile", columnList = "mobile_number"),
        @Index(name = "idx_contributor_building", columnList = "building_id")
    }
)
public class Contributor extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /** Stored in E.164 format, e.g. +919812345678. Required for WhatsApp sends. */
    @Column(name = "mobile_number", nullable = false, length = 20)
    private String mobileNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @Column(name = "floor_number", length = 20)
    private String floorNumber;

    @Column(name = "flat_number", length = 20)
    private String flatNumber;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /** Communication consent - broadcasts must only target opted-in contributors. */
    @Column(name = "whatsapp_opt_in", nullable = false)
    @Builder.Default
    private boolean whatsappOptIn = true;
}
