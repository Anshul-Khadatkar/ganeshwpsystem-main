package com.example.ganpativargani.dto.whatsapp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DonationSendRequest {
    /** Optional - falls back to the default active DONATION_THANK_YOU template. */
    private Long templateId;

    /** Optional - overrides the template's own image for this one send. */
    private String imageUrlOverride;
}
