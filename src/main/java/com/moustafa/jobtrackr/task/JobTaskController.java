package com.moustafa.jobtrackr.task;

import com.moustafa.jobtrackr.task.dto.CreateJobTaskRequest;
import com.moustafa.jobtrackr.task.dto.JobTaskResponse;
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
public class JobTaskController {

    private final JobTaskService jobTaskService;

    @GetMapping("/api/applications/{applicationId}/tasks")
    public List<JobTaskResponse> findAllForApplication(@PathVariable Long applicationId) {
        return jobTaskService.findAllForApplication(applicationId);
    }

    @PostMapping("/api/applications/{applicationId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public JobTaskResponse create(
            @PathVariable Long applicationId,
            @Valid @RequestBody CreateJobTaskRequest request
    ) {
        return jobTaskService.create(applicationId, request);
    }

    @PatchMapping("/api/tasks/{id}/complete")
    public JobTaskResponse complete(@PathVariable Long id) {
        return jobTaskService.complete(id);
    }

    @DeleteMapping("/api/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        jobTaskService.delete(id);
    }
}
