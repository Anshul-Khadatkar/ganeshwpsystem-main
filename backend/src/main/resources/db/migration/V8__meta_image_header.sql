-- ============================================================
-- V8: Track whether the Meta-approved template has an IMAGE header
-- ============================================================

ALTER TABLE message_templates
    ADD COLUMN meta_has_image_header BOOLEAN NOT NULL DEFAULT FALSE AFTER meta_category;
