package com.example.ganpativargani.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class DonationCreateRequest {

    // --- Contributor identification ---
    @NotBlank(message = "Contributor name is required")
    private String name;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Mobile number must be 10-15 digits, optionally prefixed with +")
    private String mobileNumber;

    @NotNull(message = "Building is required")
    private Long buildingId;

    /** Required only if the selected building has requiresFloor = true. Validated in DonationService. */
    private String floorNumber;

    /** Required only if the selected building has requiresFlat = true. Validated in DonationService. */
    private String flatNumber;

    // --- Donation details ---
    @NotNull(message = "Donation amount is required")
    @DecimalMin(value = "0.01", message = "Donation amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Donation date is required")
    private LocalDate donationDate;

    /** Always optional - never validated as required. */
    private String description;

    /** If true, a WhatsApp thank-you send will be attempted after saving (wired up in the WhatsApp module). */
    private boolean sendWhatsAppThankYou = false;

    /**
     * Optional. When {@link #sendWhatsAppThankYou} is true, send using this template
     * (must be an active DONATION_THANK_YOU). Falls back to the default active
     * donation thank-you template when null.
     */
    private Long thankYouTemplateId;
}
