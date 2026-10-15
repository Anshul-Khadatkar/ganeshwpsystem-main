package com.example.ganpativargani.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a donation "category" (an apartment tower, Row Houses, Shops, etc).
 * <p>
 * IMPORTANT: nothing in the codebase should ever branch on the building
 * {@code name} (e.g. {@code if (name.equals("Row Houses"))}). Instead, every
 * category declares its own location requirements via
 * {@link #requiresFloor} / {@link #requiresFlat}, so new categories can be
 * added purely through data (the Buildings admin page) without a code
 * change or redeploy.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
@Entity
@Table(name = "buildings")
public class Building extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /** Whether contributors under this category are expected to have a floor number. */
    @Column(name = "requires_floor", nullable = false)
    @Builder.Default
    private boolean requiresFloor = true;

    /** Whether contributors under this category are expected to have a flat number. */
    @Column(name = "requires_flat", nullable = false)
    @Builder.Default
    private boolean requiresFlat = true;

    /** Display order on selectors / dashboard tabs. */
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
