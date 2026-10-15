package com.example.ganpativargani.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    @Builder.Default
    private final boolean success = false;

    private final String message;

    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();

    /** Optional per-field validation messages, e.g. {"mobileNumber": "must be a valid phone number"} */
    private final List<FieldError> fieldErrors;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class FieldError {
        private final String field;
        private final String message;
    }
}
