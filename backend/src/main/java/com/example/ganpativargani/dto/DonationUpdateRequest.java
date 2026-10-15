package com.example.ganpativargani.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class DonationUpdateRequest {

    @NotNull(message = "Donation amount is required")
    @DecimalMin(value = "0.01", message = "Donation amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Donation date is required")
    private LocalDate donationDate;

    /** Always optional - never validated as required. */
    private String description;
}
