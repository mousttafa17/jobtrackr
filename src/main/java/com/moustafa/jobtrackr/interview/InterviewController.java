package com.moustafa.jobtrackr.interview;

import com.moustafa.jobtrackr.interview.dto.CreateInterviewRequest;
import com.moustafa.jobtrackr.interview.dto.InterviewResponse;
import com.moustafa.jobtrackr.interview.dto.UpdateInterviewRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Interviews", description = "Manage interviews for job applications")
public class InterviewController {

    private final InterviewService interviewService;

    @Operation(summary = "List interviews for a job application")
    @GetMapping("/api/applications/{applicationId}/interviews")
    public List<InterviewResponse> findAllForApplication(@PathVariable Long applicationId) {
        return interviewService.findAllForApplication(applicationId);
    }

    @Operation(summary = "Create an interview for a job application")
    @PostMapping("/api/applications/{applicationId}/interviews")
    @ResponseStatus(HttpStatus.CREATED)
    public InterviewResponse create(
            @PathVariable Long applicationId,
            @Valid @RequestBody CreateInterviewRequest request
    ) {
        return interviewService.create(applicationId, request);
    }

    @Operation(summary = "Update an interview")
    @PutMapping("/api/interviews/{id}")
    public InterviewResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInterviewRequest request
    ) {
        return interviewService.update(id, request);
    }

    @Operation(summary = "Delete an interview")
    @DeleteMapping("/api/interviews/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        interviewService.delete(id);
    }
}
