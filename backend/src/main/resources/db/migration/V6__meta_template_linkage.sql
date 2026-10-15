-- ============================================================
-- V6: Link app-level message templates to Meta-approved templates,
-- enabling business-initiated sends outside the 24-hour messaging
-- window (requires the template to be approved in Meta Business
-- Manager first - see README section on WhatsApp template setup).
-- ============================================================

ALTER TABLE message_templates
    ADD COLUMN meta_template_name    VARCHAR(200) NULL,
    ADD COLUMN meta_language_code    VARCHAR(10)  NULL DEFAULT 'en_US',
    ADD COLUMN meta_parameter_format VARCHAR(20)  NULL DEFAULT 'NAMED';
