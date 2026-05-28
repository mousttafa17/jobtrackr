package com.moustafa.jobtrackr.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateNoteRequest(
        @NotBlank(message = "Note content is required")
        @Size(max = 5000, message = "Note content must be at most 5000 characters")
        String content
) {
}
