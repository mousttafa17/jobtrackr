package com.moustafa.jobtrackr.application.dto;

import com.moustafa.jobtrackr.application.ApplicationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record JobApplicationResponse(
        Long id,
        Long userId,
        String companyName,
        String jobTitle,
        String location,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        String jobLink,
        ApplicationStatus status,
        LocalDate applicationDate,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
