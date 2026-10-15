package com.example.ganpativargani.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A single broadcast send-out. {@code message}/{@code imageUrl} are a
 * SNAPSHOT of what was actually sent (copied from the template, if any,
 * at send time) so editing a template later never rewrites history.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
@Entity
@Table(name = "broadcasts")
public class Broadcast extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private MessageTemplate template;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "created_by", length = 150)
    private String createdBy;

    /** Human-readable description of how recipients were chosen, e.g. "Sunflower-A, donated ≥ ₹2,500". */
    @Column(name = "recipient_criteria", length = 500)
    private String recipientCriteria;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private BroadcastStatus status = BroadcastStatus.PENDING;

    @Column(name = "total_recipients", nullable = false)
    @Builder.Default
    private int totalRecipients = 0;

    @Column(name = "successful_count", nullable = false)
    @Builder.Default
    private int successfulCount = 0;

    @Column(name = "failed_count", nullable = false)
    @Builder.Default
    private int failedCount = 0;
}
