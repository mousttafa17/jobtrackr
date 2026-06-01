package com.moustafa.jobtrackr.application.dto;

import com.moustafa.jobtrackr.application.ApplicationStatus;

import java.time.Instant;

public record JobApplicationStatusHistoryResponse(
        Long id,
        Long applicationId,
        ApplicationStatus oldStatus,
        ApplicationStatus newStatus,
        Instant changedAt
) {
}
