package com.example.ganpativargani.whatsapp;

/** Normalizes phone numbers for Meta WhatsApp Cloud API (E.164 digits only, no +). */
public final class WhatsAppPhoneNormalizer {

    private WhatsAppPhoneNormalizer() {
    }

    public static String forMeta(String phone) {
        if (phone == null) {
            return "";
        }
        String digits = phone.replaceAll("\\D", "");
        // India: allow 10-digit local numbers by prefixing 91.
        if (digits.length() == 10) {
            return "91" + digits;
        }
        return digits;
    }
}
