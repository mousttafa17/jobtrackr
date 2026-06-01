package com.moustafa.jobtrackr.document;

import com.moustafa.jobtrackr.document.dto.CreateJobDocumentRequest;
import com.moustafa.jobtrackr.document.dto.JobDocumentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Manage document links for job applications")
public class JobDocumentController {

    private final JobDocumentService jobDocumentService;

    @Operation(summary = "List documents for a job application")
    @GetMapping("/api/applications/{applicationId}/documents")
    public List<JobDocumentResponse> findAllForApplication(@PathVariable Long applicationId) {
        return jobDocumentService.findAllForApplication(applicationId);
    }

    @Operation(summary = "Create a document link for a job application")
    @PostMapping("/api/applications/{applicationId}/documents")
    @ResponseStatus(HttpStatus.CREATED)
    public JobDocumentResponse create(
            @PathVariable Long applicationId,
            @Valid @RequestBody CreateJobDocumentRequest request
    ) {
        return jobDocumentService.create(applicationId, request);
    }

    @Operation(summary = "Delete a document")
    @DeleteMapping("/api/documents/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        jobDocumentService.delete(id);
    }
}
