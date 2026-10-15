package com.example.ganpativargani.common;

/**
 * Normalizes mobile numbers to a consistent E.164-style format
 * (e.g. "9812345678" -> "+919812345678") so the same contributor
 * is matched reliably and WhatsApp Cloud API sends (which require
 * E.164) never fail on formatting alone.
 *
 * NOTE: defaults to the +91 (India) country code, matching this
 * application's context. If this system is ever used outside India,
 * make the default country code an environment-driven setting.
 */
public final class PhoneNumberUtil {

    private static final String DEFAULT_COUNTRY_CODE = "+91";

    private PhoneNumberUtil() {
    }

    public static String normalize(String rawNumber) {
        if (rawNumber == null) {
            return null;
        }
        String digitsOnly = rawNumber.replaceAll("[^0-9+]", "");

        if (digitsOnly.startsWith("+")) {
            return digitsOnly;
        }
        if (digitsOnly.length() == 10) {
            return DEFAULT_COUNTRY_CODE + digitsOnly;
        }
        if (digitsOnly.length() > 10) {
            // Already has a country code but was missing the '+'.
            return "+" + digitsOnly;
        }
        return digitsOnly;
    }
}
