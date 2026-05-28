package com.moustafa.jobtrackr.interview;

import com.moustafa.jobtrackr.application.ApplicationStatus;
import com.moustafa.jobtrackr.application.JobApplication;
import com.moustafa.jobtrackr.application.JobApplicationRepository;
import com.moustafa.jobtrackr.common.exception.ResourceNotFoundException;
import com.moustafa.jobtrackr.interview.dto.CreateInterviewRequest;
import com.moustafa.jobtrackr.interview.dto.InterviewResponse;
import com.moustafa.jobtrackr.interview.dto.UpdateInterviewRequest;
import com.moustafa.jobtrackr.security.AuthenticatedUserProvider;
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
class InterviewServiceTest {

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    private InterviewService interviewService;
    private User user;
    private JobApplication application;

    @BeforeEach
    void setUp() {
        interviewService = new InterviewService(interviewRepository, jobApplicationRepository, authenticatedUserProvider);
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
    void findAllForApplicationReturnsOwnedInterviews() {
        Interview interview = Interview.builder()
                .id(20L)
                .application(application)
                .type(InterviewType.TECHNICAL)
                .status(InterviewStatus.SCHEDULED)
                .scheduledAt(Instant.parse("2026-06-01T15:00:00Z"))
                .createdAt(Instant.parse("2026-05-28T05:30:00Z"))
                .updatedAt(Instant.parse("2026-05-28T05:30:00Z"))
                .build();

        when(authenticatedUserProvider.getCurrentUser()).thenReturn(user);
        when(jobApplicationRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(application));
        when(interviewRepository.findAllByApplication_IdAndApplication_UserOrderByScheduledAtAsc(10L, user))
                .thenReturn(List.of(interview));

        List<InterviewResponse> response = interviewService.findAllForApplication(10L);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().id()).isEqualTo(20L);
        assertThat(response.getFirst().applicationId()).isEqualTo(10L);
        assertThat(response.getFirst().type()).isEqualTo(InterviewType.TECHNICAL);
    }

    @Test
    void createStoresInterviewForOwnedApplication() {
        CreateInterviewRequest request = new CreateInterviewRequest(
                InterviewType.PHONE_SCREEN,
                null,
                Instant.parse("2026-06-02T14:00:00Z"),
                "Remote",
                "https://meet.example.com/jobtrackr",
                "Jane Recruiter",
                "Prepare company story"
        );

        when(authenticatedUserProvider.getCurrentUser()).thenReturn(user);
        when(jobApplicationRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(application));
        when(interviewRepository.save(any(Interview.class))).thenAnswer(invocation -> {
            Interview saved = invocation.getArgument(0);
            saved.setId(30L);
            saved.setStatus(InterviewStatus.SCHEDULED);
            saved.setCreatedAt(Instant.parse("2026-05-28T06:00:00Z"));
            saved.setUpdatedAt(Instant.parse("2026-05-28T06:00:00Z"));
            return saved;
        });

        InterviewResponse response = interviewService.create(10L, request);

        assertThat(response.id()).isEqualTo(30L);
        assertThat(response.applicationId()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(InterviewStatus.SCHEDULED);

        ArgumentCaptor<Interview> interviewCaptor = ArgumentCaptor.forClass(Interview.class);
        verify(interviewRepository).save(interviewCaptor.capture());
        assertThat(interviewCaptor.getValue().getApplication()).isEqualTo(application);
        assertThat(interviewCaptor.getValue().getType()).isEqualTo(InterviewType.PHONE_SCREEN);
    }

    @Test
    void createRejectsInterviewForApplicationNotOwnedByCurrentUser() {
        CreateInterviewRequest request = new CreateInterviewRequest(
                InterviewType.TECHNICAL,
                InterviewStatus.SCHEDULED,
                Instant.parse("2026-06-03T14:00:00Z"),
                null,
                null,
                null,
                null
        );

        when(authenticatedUserProvider.getCurrentUser()).thenReturn(user);
        when(jobApplicationRepository.findByIdAndUser(999L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.create(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Job application not found");

        verify(interviewRepository, never()).save(any(Interview.class));
    }

    @Test
    void updateRejectsInterviewNotOwnedByCurrentUser() {
        UpdateInterviewRequest request = new UpdateInterviewRequest(
                InterviewType.FINAL,
                InterviewStatus.RESCHEDULED,
                Instant.parse("2026-06-04T14:00:00Z"),
                null,
                null,
                null,
                null
        );

        when(authenticatedUserProvider.getCurrentUser()).thenReturn(user);
        when(interviewRepository.findByIdAndApplication_User(404L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.update(404L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Interview not found");

        verify(interviewRepository, never()).saveAndFlush(any(Interview.class));
    }
}
