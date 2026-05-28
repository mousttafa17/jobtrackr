package com.moustafa.jobtrackr.application.dto;

import com.moustafa.jobtrackr.application.ApplicationStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateJobApplicationRequest(
        @NotBlank(message = "Company name is required")
        @Size(max = 120, message = "Company name must be at most 120 characters")
        String companyName,

        @NotBlank(message = "Job title is required")
        @Size(max = 120, message = "Job title must be at most 120 characters")
        String jobTitle,

        @Size(max = 120, message = "Location must be at most 120 characters")
        String location,

        @DecimalMin(value = "0.0", inclusive = true, message = "Minimum salary cannot be negative")
        BigDecimal salaryMin,

        @DecimalMin(value = "0.0", inclusive = true, message = "Maximum salary cannot be negative")
        BigDecimal salaryMax,

        @Size(max = 2048, message = "Job link must be at most 2048 characters")
        String jobLink,

        ApplicationStatus status,

        LocalDate applicationDate,

        @Size(max = 5000, message = "Notes must be at most 5000 characters")
        String notes
) {
}
