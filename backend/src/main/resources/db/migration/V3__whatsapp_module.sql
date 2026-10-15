-- ============================================================
-- V3: WhatsApp communication module + app-level settings.
-- ============================================================

CREATE TABLE message_templates (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(150)    NOT NULL,
    message         TEXT            NOT NULL,
    image_url       VARCHAR(1000)   NULL,
    type            VARCHAR(30)     NOT NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      DATETIME        NOT NULL,
    updated_at      DATETIME        NOT NULL,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at      DATETIME        NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE broadcasts (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id         BIGINT          NULL,
    message             TEXT            NOT NULL,
    image_url           VARCHAR(1000)   NULL,
    created_by          VARCHAR(150)    NULL,
    recipient_criteria  VARCHAR(500)    NULL,
    status              VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
    total_recipients    INT             NOT NULL DEFAULT 0,
    successful_count    INT             NOT NULL DEFAULT 0,
    failed_count        INT             NOT NULL DEFAULT 0,
    created_at          DATETIME        NOT NULL,
    updated_at          DATETIME        NOT NULL,
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at          DATETIME        NULL,
    CONSTRAINT fk_broadcast_template FOREIGN KEY (template_id) REFERENCES message_templates(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE broadcast_recipients (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    broadcast_id            BIGINT          NOT NULL,
    contributor_id          BIGINT          NOT NULL,
    phone_number            VARCHAR(20)     NOT NULL,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    provider_message_id     VARCHAR(100)    NULL,
    error_message           TEXT            NULL,
    sent_at                 DATETIME        NULL,
    created_at              DATETIME        NOT NULL,
    updated_at              DATETIME        NOT NULL,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at              DATETIME        NULL,
    CONSTRAINT fk_broadcast_recipient_broadcast FOREIGN KEY (broadcast_id) REFERENCES broadcasts(id),
    CONSTRAINT fk_broadcast_recipient_contributor FOREIGN KEY (contributor_id) REFERENCES contributors(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_broadcast_recipient_broadcast ON broadcast_recipients (broadcast_id);
CREATE INDEX idx_broadcast_recipient_status ON broadcast_recipients (status);

CREATE TABLE app_settings (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key     VARCHAR(100)    NOT NULL,
    setting_value   TEXT            NULL,
    updated_at      DATETIME        NOT NULL,
    CONSTRAINT uq_app_setting_key UNIQUE (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Seed a starter set of templates so the app is usable immediately.
-- All fully admin-editable afterwards from WhatsApp > Templates -
-- nothing here is hardcoded into application code.
-- ============================================================

INSERT INTO message_templates (name, message, image_url, type, active, created_at, updated_at, deleted)
VALUES
(
    'Donation Thank You',
    '🙏 Thank you {{name}} for your generous contribution!\n\nDonation Details:\n🏢 Building: {{building}}\n🏠 Flat: {{flat}}\n💰 Vargani: ₹{{amount}}\n📅 Date: {{date}}\n\nYour contribution means a lot to us.\n\nGanpati Bappa Morya! 🙏',
    NULL,
    'DONATION_THANK_YOU',
    TRUE,
    NOW(), NOW(), FALSE
),
(
    'Aarti Started',
    '🙏 Aarti has started!\n\nPlease join us at the Ganpati pandal.\n\nGanpati Bappa Morya! 🙏',
    NULL,
    'ANNOUNCEMENT',
    TRUE,
    NOW(), NOW(), FALSE
),
(
    'Drawing Competition',
    '🎨 Today we are organizing a drawing competition!\n\nPlease bring your children and join us at the venue.\n\nTime: 5:00 PM\n\nGanpati Bappa Morya! 🙏',
    NULL,
    'ANNOUNCEMENT',
    TRUE,
    NOW(), NOW(), FALSE
);

INSERT INTO app_settings (setting_key, setting_value, updated_at)
VALUES ('organizationName', 'Ganpati Mandal', NOW());
