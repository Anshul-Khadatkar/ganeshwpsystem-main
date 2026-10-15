-- ============================================================
-- V5: Indexes supporting WhatsApp webhook status reconciliation
-- (lookup by provider message id, added when the webhook receiver
-- was built).
-- ============================================================

CREATE INDEX idx_donation_whatsapp_message_id ON donations (whatsapp_message_id);
CREATE INDEX idx_broadcast_recipient_provider_message_id ON broadcast_recipients (provider_message_id);
