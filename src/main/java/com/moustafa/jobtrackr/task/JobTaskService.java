package com.moustafa.jobtrackr.task;

import com.moustafa.jobtrackr.application.JobApplication;
import com.moustafa.jobtrackr.application.JobApplicationRepository;
import com.moustafa.jobtrackr.common.exception.ResourceNotFoundException;
import com.moustafa.jobtrackr.security.AuthenticatedUserProvider;
import com.moustafa.jobtrackr.task.dto.CreateJobTaskRequest;
import com.moustafa.jobtrackr.task.dto.JobTaskResponse;
import com.moustafa.jobtrackr.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobTaskService {

    private final JobTaskRepository jobTaskRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional(readOnly = true)
    public List<JobTaskResponse> findAllForApplication(Long applicationId) {
        User user = authenticatedUserProvider.getCurrentUser();
        ensureOwnedApplicationExists(applicationId, user);

        return jobTaskRepository.findAllByApplication_IdAndApplication_UserOrderByCompletedAscDueAtAsc(
                        applicationId,
                        user
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public JobTaskResponse create(Long applicationId, CreateJobTaskRequest request) {
        User user = authenticatedUserProvider.getCurrentUser();
        JobApplication application = getOwnedApplication(applicationId, user);

        JobTask task = JobTask.builder()
                .application(application)
                .title(request.title())
                .description(request.description())
                .dueAt(request.dueAt())
                .completed(false)
                .build();

        return toResponse(jobTaskRepository.save(task));
    }

    @Transactional
    public JobTaskResponse complete(Long id) {
        JobTask task = getOwnedTask(id);
        if (!task.isCompleted()) {
            task.setCompleted(true);
            task.setCompletedAt(Instant.now());
        }

        return toResponse(jobTaskRepository.saveAndFlush(task));
    }

    @Transactional
    public void delete(Long id) {
        jobTaskRepository.delete(getOwnedTask(id));
    }

    private void ensureOwnedApplicationExists(Long applicationId, User user) {
        if (jobApplicationRepository.findByIdAndUser(applicationId, user).isEmpty()) {
            throw new ResourceNotFoundException("Job application not found");
        }
    }

    private JobApplication getOwnedApplication(Long applicationId, User user) {
        return jobApplicationRepository.findByIdAndUser(applicationId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Job application not found"));
    }

    private JobTask getOwnedTask(Long id) {
        User user = authenticatedUserProvider.getCurrentUser();
        return jobTaskRepository.findByIdAndApplication_User(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private JobTaskResponse toResponse(JobTask task) {
        return new JobTaskResponse(
                task.getId(),
                task.getApplication().getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueAt(),
                task.isCompleted(),
                task.getCompletedAt(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
