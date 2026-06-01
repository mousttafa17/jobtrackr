package com.moustafa.jobtrackr.document.dto;

import com.moustafa.jobtrackr.document.DocumentType;

import java.time.Instant;

public record JobDocumentResponse(
        Long id,
        Long applicationId,
        String name,
        DocumentType type,
        String url,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
