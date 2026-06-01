package com.moustafa.jobtrackr.task;

import com.moustafa.jobtrackr.task.dto.CreateJobTaskRequest;
import com.moustafa.jobtrackr.task.dto.JobTaskResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Manage follow-up tasks and reminders for job applications")
public class JobTaskController {

    private final JobTaskService jobTaskService;

    @Operation(summary = "List tasks for a job application")
    @GetMapping("/api/applications/{applicationId}/tasks")
    public List<JobTaskResponse> findAllForApplication(@PathVariable Long applicationId) {
        return jobTaskService.findAllForApplication(applicationId);
    }

    @Operation(summary = "Create a task for a job application")
    @PostMapping("/api/applications/{applicationId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public JobTaskResponse create(
            @PathVariable Long applicationId,
            @Valid @RequestBody CreateJobTaskRequest request
    ) {
        return jobTaskService.create(applicationId, request);
    }

    @Operation(summary = "Mark a task complete")
    @PatchMapping("/api/tasks/{id}/complete")
    public JobTaskResponse complete(@PathVariable Long id) {
        return jobTaskService.complete(id);
    }

    @Operation(summary = "Delete a task")
    @DeleteMapping("/api/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        jobTaskService.delete(id);
    }
}
