-- V7: Track Meta template submission and approval status from the app UI.

ALTER TABLE message_templates
    ADD COLUMN meta_status           VARCHAR(20)  NULL DEFAULT 'NOT_SUBMITTED',
    ADD COLUMN meta_rejection_reason TEXT         NULL,
    ADD COLUMN meta_category         VARCHAR(20)  NULL DEFAULT 'UTILITY';
