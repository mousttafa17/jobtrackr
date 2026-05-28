package com.moustafa.jobtrackr.application.dto;

import com.moustafa.jobtrackr.application.ApplicationStatus;

public record JobApplicationFilter(
        ApplicationStatus status,
        String company,
        String search
) {
}
