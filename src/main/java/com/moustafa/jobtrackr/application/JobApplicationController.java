package com.moustafa.jobtrackr.application;

import com.moustafa.jobtrackr.application.dto.CreateJobApplicationRequest;
import com.moustafa.jobtrackr.application.dto.JobApplicationFilter;
import com.moustafa.jobtrackr.application.dto.JobApplicationResponse;
import com.moustafa.jobtrackr.application.dto.JobApplicationStatusHistoryResponse;
import com.moustafa.jobtrackr.application.dto.UpdateApplicationStatusRequest;
import com.moustafa.jobtrackr.application.dto.UpdateJobApplicationRequest;
import com.moustafa.jobtrackr.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Tag(name = "Applications", description = "Manage job applications and status history")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @Operation(summary = "List job applications", description = "Returns the current user's applications with optional filtering, pagination, and sorting.")
    @GetMapping
    public PageResponse<JobApplicationResponse> findAll(
            JobApplicationFilter filter,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return jobApplicationService.findAllForCurrentUser(filter, pageable);
    }

    @Operation(summary = "Create a job application")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobApplicationResponse create(@Valid @RequestBody CreateJobApplicationRequest request) {
        return jobApplicationService.create(request);
    }

    @Operation(summary = "Get a job application")
    @GetMapping("/{id}")
    public JobApplicationResponse findById(@PathVariable Long id) {
        return jobApplicationService.findByIdForCurrentUser(id);
    }

    @Operation(summary = "List application status history")
    @GetMapping("/{id}/status-history")
    public List<JobApplicationStatusHistoryResponse> findStatusHistory(@PathVariable Long id) {
        return jobApplicationService.findStatusHistory(id);
    }

    @Operation(summary = "Update a job application")
    @PutMapping("/{id}")
    public JobApplicationResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobApplicationRequest request
    ) {
        return jobApplicationService.update(id, request);
    }

    @Operation(summary = "Update application status")
    @PatchMapping("/{id}/status")
    public JobApplicationResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateApplicationStatusRequest request
    ) {
        return jobApplicationService.updateStatus(id, request);
    }

    @Operation(summary = "Delete a job application")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        jobApplicationService.delete(id);
    }
}
