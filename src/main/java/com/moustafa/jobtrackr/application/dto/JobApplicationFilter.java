package com.moustafa.jobtrackr.application.dto;

import com.moustafa.jobtrackr.application.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record JobApplicationFilter(
        @Schema(description = "Filter by application status", example = "INTERVIEW")
        ApplicationStatus status,

        @Schema(description = "Case-insensitive company name search", example = "Google")
        String company,

        @Schema(description = "Case-insensitive search across company, title, location, and notes", example = "backend")
        String search
) {
}
