package com.example.ganpativargani.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Append-only record of significant admin actions (donation added/edited,
 * broadcast sent, building deactivated, etc). Deliberately NOT extending
 * BaseEntity - audit rows are never soft-deleted or updated themselves.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;

    @Column(name = "entity_id")
    private Long entityId;

    /** e.g. CREATE, UPDATE, DELETE, WHATSAPP_SEND, WHATSAPP_RETRY, BROADCAST_SEND */
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "performed_by", length = 150)
    private String performedBy;

    @Column(name = "performed_at", nullable = false)
    @Builder.Default
    private LocalDateTime performedAt = LocalDateTime.now();

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;
}
