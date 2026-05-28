package com.moustafa.jobtrackr.task.dto;

import java.time.Instant;

public record JobTaskResponse(
        Long id,
        Long applicationId,
        String title,
        String description,
        Instant dueAt,
        boolean completed,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
