package com.moustafa.jobtrackr.application.dto;

import com.moustafa.jobtrackr.application.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateApplicationStatusRequest(
        @NotNull(message = "Status is required")
        ApplicationStatus status
) {
}
