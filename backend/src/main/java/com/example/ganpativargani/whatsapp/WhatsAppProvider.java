package com.example.ganpativargani.whatsapp;

import com.example.ganpativargani.entity.MetaParameterFormat;

import java.util.Map;

/**
 * Abstraction over "however we actually deliver a WhatsApp message today".
 * {@link com.example.ganpativargani.service.WhatsAppService} and
 * {@link com.example.ganpativargani.service.BroadcastService} depend ONLY
 * on this interface - never on Meta's API shape directly - so swapping
 * providers (mock during development, Meta Cloud API in production, or a
 * different provider entirely later) is a one-line config change
 * (`app.whatsapp.provider`), not a code change.
 */
public interface WhatsAppProvider {

    /** Free-form text/image send. Only deliverable inside Meta's 24-hour customer service window. */
    WhatsAppSendResult send(WhatsAppMessageRequest request);

    /**
     * Business-initiated send using a Meta-approved template. Deliverable
     * to any recipient at any time, since Meta has pre-vetted the content.
     *
     * @param namedParameters variable name -> value, e.g. "name" -> "Rahul".
     *                        Missing/blank values are the caller's
     *                        responsibility to fill with a placeholder -
     *                        Meta rejects empty template parameters outright.
     */
    WhatsAppSendResult sendTemplate(String toPhoneNumber, String templateName, String languageCode,
                                     MetaParameterFormat parameterFormat, Map<String, String> namedParameters,
                                     String headerImageUrl);
}
