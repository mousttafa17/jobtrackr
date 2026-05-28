package com.moustafa.jobtrackr.task;

import com.moustafa.jobtrackr.application.ApplicationStatus;
import com.moustafa.jobtrackr.application.JobApplication;
import com.moustafa.jobtrackr.application.JobApplicationRepository;
import com.moustafa.jobtrackr.common.exception.ResourceNotFoundException;
import com.moustafa.jobtrackr.security.AuthenticatedUserProvider;
import com.moustafa.jobtrackr.task.dto.CreateJobTaskRequest;
import com.moustafa.jobtrackr.task.dto.JobTaskResponse;
import com.moustafa.jobtrackr.user.Role;
import com.moustafa.jobtrackr.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobTaskServiceTest {

    @Mock
    private JobTaskRepository jobTaskRepository;

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    private JobTaskService jobTaskService;
    private User user;
    private JobApplication application;

    @BeforeEach
    void setUp() {
        jobTaskService = new JobTaskService(jobTaskRepository, jobApplicationRepository, authenticatedUserProvider);
        user = User.builder()
                .id(1L)
                .fullName("Demo User")
                .email("demo@jobtrackr.com")
                .passwordHash("hash")
                .role(Role.USER)
                .createdAt(Instant.now())
                .build();
        application = JobApplication.builder()
                .id(10L)
                .user(user)
                .companyName("OpenAI")
                .jobTitle("Backend Engineer")
                .status(ApplicationStatus.INTERVIEW)
                .createdAt(Instant.parse("2026-05-28T05:00:00Z"))
                .updatedAt(Instant.parse("2026-05-28T05:00:00Z"))
                .build();
    }

    @Test
    void findAllForApplicationReturnsOwnedTasks() {
        JobTask task = JobTask.builder()
                .id(20L)
                .application(application)
                .title("Send thank-you email")
                .description("Mention the API design discussion")
                .dueAt(Instant.parse("2026-06-01T15:00:00Z"))
                .completed(false)
                .createdAt(Instant.parse("2026-05-28T05:30:00Z"))
                .updatedAt(Instant.parse("2026-05-28T05:30:00Z"))
                .build();

        when(authenticatedUserProvider.getCurrentUser()).thenReturn(user);
        when(jobApplicationRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(application));
        when(jobTaskRepository.findAllByApplication_IdAndApplication_UserOrderByCompletedAscDueAtAsc(10L, user))
                .thenReturn(List.of(task));

        List<JobTaskResponse> response = jobTaskService.findAllForApplication(10L);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().id()).isEqualTo(20L);
        assertThat(response.getFirst().applicationId()).isEqualTo(10L);
        assertThat(response.getFirst().title()).isEqualTo("Send thank-you email");
        assertThat(response.getFirst().completed()).isFalse();
    }

    @Test
    void createStoresTaskForOwnedApplication() {
        CreateJobTaskRequest request = new CreateJobTaskRequest(
                "Prepare system design notes",
                "Review pagination and auth tradeoffs",
                Instant.parse("2026-06-02T14:00:00Z")
        );

        when(authenticatedUserProvider.getCurrentUser()).thenReturn(user);
        when(jobApplicationRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(application));
        when(jobTaskRepository.save(any(JobTask.class))).thenAnswer(invocation -> {
            JobTask saved = invocation.getArgument(0);
            saved.setId(30L);
            saved.setCreatedAt(Instant.parse("2026-05-28T06:00:00Z"));
            saved.setUpdatedAt(Instant.parse("2026-05-28T06:00:00Z"));
            return saved;
        });

        JobTaskResponse response = jobTaskService.create(10L, request);

        assertThat(response.id()).isEqualTo(30L);
        assertThat(response.applicationId()).isEqualTo(10L);
        assertThat(response.completed()).isFalse();

        ArgumentCaptor<JobTask> taskCaptor = ArgumentCaptor.forClass(JobTask.class);
        verify(jobTaskRepository).save(taskCaptor.capture());
        assertThat(taskCaptor.getValue().getApplication()).isEqualTo(application);
        assertThat(taskCaptor.getValue().getTitle()).isEqualTo("Prepare system design notes");
    }

    @Test
    void completeMarksOwnedTaskCompleted() {
        JobTask task = JobTask.builder()
                .id(40L)
                .application(application)
                .title("Follow up")
                .completed(false)
                .createdAt(Instant.parse("2026-05-28T06:00:00Z"))
                .updatedAt(Instant.parse("2026-05-28T06:00:00Z"))
                .build();

        when(authenticatedUserProvider.getCurrentUser()).thenReturn(user);
        when(jobTaskRepository.findByIdAndApplication_User(40L, user)).thenReturn(Optional.of(task));
        when(jobTaskRepository.saveAndFlush(task)).thenReturn(task);

        JobTaskResponse response = jobTaskService.complete(40L);

        assertThat(response.completed()).isTrue();
        assertThat(response.completedAt()).isNotNull();
        verify(jobTaskRepository).saveAndFlush(task);
    }

    @Test
    void createRejectsTaskForApplicationNotOwnedByCurrentUser() {
        CreateJobTaskRequest request = new CreateJobTaskRequest(
                "Follow up",
                null,
                null
        );

        when(authenticatedUserProvider.getCurrentUser()).thenReturn(user);
        when(jobApplicationRepository.findByIdAndUser(999L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobTaskService.create(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Job application not found");

        verify(jobTaskRepository, never()).save(any(JobTask.class));
    }

    @Test
    void completeRejectsTaskNotOwnedByCurrentUser() {
        when(authenticatedUserProvider.getCurrentUser()).thenReturn(user);
        when(jobTaskRepository.findByIdAndApplication_User(404L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobTaskService.complete(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Task not found");

        verify(jobTaskRepository, never()).saveAndFlush(any(JobTask.class));
    }
}
