package com.moustafa.jobtrackr.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateJobTaskRequest(
        @NotBlank(message = "Task title is required")
        @Size(max = 160, message = "Task title must be at most 160 characters")
        String title,

        @Size(max = 5000, message = "Description must be at most 5000 characters")
        String description,

        Instant dueAt
) {
}
