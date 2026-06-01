package com.moustafa.jobtrackr.document.dto;

import com.moustafa.jobtrackr.document.DocumentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateJobDocumentRequest(
        @Schema(example = "Google backend resume")
        @NotBlank(message = "Document name is required")
        @Size(max = 120, message = "Document name must be at most 120 characters")
        String name,

        @Schema(example = "RESUME")
        DocumentType type,

        @Schema(example = "https://drive.google.com/file/d/example")
        @NotBlank(message = "Document URL is required")
        @Size(max = 2048, message = "Document URL must be at most 2048 characters")
        String url,

        @Schema(example = "Tailored resume emphasizing Java and Spring Boot.")
        @Size(max = 2000, message = "Document notes must be at most 2000 characters")
        String notes
) {
}
