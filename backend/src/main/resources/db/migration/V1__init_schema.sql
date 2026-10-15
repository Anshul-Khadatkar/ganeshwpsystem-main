-- ============================================================
-- V1: Core schema - buildings, contributors, donations, audit
-- WhatsApp tables (message_templates, broadcasts, ...) are added
-- in a later migration once that module is built (Session 5).
-- ============================================================

CREATE TABLE buildings (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    description     TEXT            NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    requires_floor  BOOLEAN         NOT NULL DEFAULT TRUE,
    requires_flat   BOOLEAN         NOT NULL DEFAULT TRUE,
    sort_order      INT             NOT NULL DEFAULT 0,
    created_at      DATETIME        NOT NULL,
    updated_at      DATETIME        NOT NULL,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at      DATETIME        NULL,
    CONSTRAINT uq_building_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE contributors (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(150)    NOT NULL,
    mobile_number   VARCHAR(20)     NOT NULL,
    building_id     BIGINT          NOT NULL,
    floor_number    VARCHAR(20)     NULL,
    flat_number     VARCHAR(20)     NULL,
    notes           TEXT            NULL,
    whatsapp_opt_in BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      DATETIME        NOT NULL,
    updated_at      DATETIME        NOT NULL,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at      DATETIME        NULL,
    CONSTRAINT fk_contributor_building FOREIGN KEY (building_id) REFERENCES buildings(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_contributor_mobile ON contributors (mobile_number);
CREATE INDEX idx_contributor_building ON contributors (building_id);

CREATE TABLE donations (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    contributor_id          BIGINT          NOT NULL,
    amount                  DECIMAL(12,2)   NOT NULL,
    donation_date           DATE            NOT NULL,
    description             TEXT            NULL,
    whatsapp_status         VARCHAR(20)     NOT NULL DEFAULT 'NOT_SENT',
    whatsapp_message_id     VARCHAR(100)    NULL,
    whatsapp_sent_at        DATETIME        NULL,
    whatsapp_error_message  TEXT            NULL,
    created_at              DATETIME        NOT NULL,
    updated_at              DATETIME        NOT NULL,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at              DATETIME        NULL,
    CONSTRAINT fk_donation_contributor FOREIGN KEY (contributor_id) REFERENCES contributors(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_donation_contributor ON donations (contributor_id);
CREATE INDEX idx_donation_date ON donations (donation_date);
CREATE INDEX idx_donation_whatsapp_status ON donations (whatsapp_status);

CREATE TABLE audit_logs (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_name     VARCHAR(100)    NOT NULL,
    entity_id       BIGINT          NULL,
    action          VARCHAR(50)     NOT NULL,
    performed_by    VARCHAR(150)    NULL,
    performed_at    DATETIME        NOT NULL,
    details         TEXT            NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_audit_entity ON audit_logs (entity_name, entity_id);
