package com.moustafa.jobtrackr.application.dto;

import com.moustafa.jobtrackr.application.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateJobApplicationRequest(
        @Schema(example = "Google")
        @NotBlank(message = "Company name is required")
        @Size(max = 120, message = "Company name must be at most 120 characters")
        String companyName,

        @Schema(example = "Backend Engineer")
        @NotBlank(message = "Job title is required")
        @Size(max = 120, message = "Job title must be at most 120 characters")
        String jobTitle,

        @Schema(example = "Remote")
        @Size(max = 120, message = "Location must be at most 120 characters")
        String location,

        @Schema(example = "110000")
        @DecimalMin(value = "0.0", inclusive = true, message = "Minimum salary cannot be negative")
        BigDecimal salaryMin,

        @Schema(example = "150000")
        @DecimalMin(value = "0.0", inclusive = true, message = "Maximum salary cannot be negative")
        BigDecimal salaryMax,

        @Schema(example = "https://careers.google.com/jobs/results/example")
        @Size(max = 2048, message = "Job link must be at most 2048 characters")
        String jobLink,

        @Schema(example = "APPLIED")
        ApplicationStatus status,

        @Schema(example = "2026-06-01")
        LocalDate applicationDate,

        @Schema(example = "Applied through the company careers page.")
        @Size(max = 5000, message = "Notes must be at most 5000 characters")
        String notes
) {
}
