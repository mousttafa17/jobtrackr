package com.moustafa.jobtrackr.note.dto;

import java.time.Instant;

public record NoteResponse(
        Long id,
        Long applicationId,
        String content,
        Instant createdAt,
        Instant updatedAt
) {
}
