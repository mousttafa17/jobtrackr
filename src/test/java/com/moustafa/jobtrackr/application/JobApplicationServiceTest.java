package com.moustafa.jobtrackr.application;

import com.moustafa.jobtrackr.application.dto.CreateJobApplicationRequest;
import com.moustafa.jobtrackr.application.dto.JobApplicationFilter;
import com.moustafa.jobtrackr.application.dto.JobApplicationResponse;
import com.moustafa.jobtrackr.common.exception.BadRequestException;
import com.moustafa.jobtrackr.common.response.PageResponse;
import com.moustafa.jobtrackr.security.AuthenticatedUserProvider;
import com.moustafa.jobtrackr.user.Role;
import com.moustafa.jobtrackr.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    private JobApplicationService jobApplicationService;
    private User user;

    @BeforeEach
    void setUp() {
        jobApplicationService = new JobApplicationService(jobApplicationRepository, authenticatedUserProvider);
        user = User.builder()
                .id(1L)
                .fullName("Demo User")
                .email("demo@jobtrackr.com")
                .passwordHash("hash")
                .role(Role.USER)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void findAllForCurrentUserReturnsPagedResponse() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        JobApplication application = JobApplication.builder()
                .id(10L)
                .user(user)
                .companyName("OpenAI")
                .jobTitle("Backend Engineer")
                .status(ApplicationStatus.INTERVIEW)
                .applicationDate(LocalDate.of(2026, 5, 28))
                .createdAt(Instant.parse("2026-05-28T05:00:00Z"))
                .updatedAt(Instant.parse("2026-05-28T05:00:00Z"))
                .build();

        when(authenticatedUserProvider.getCurrentUser()).thenReturn(user);
        when(jobApplicationRepository.findAll(anyJobApplicationSpecification(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(application), pageable, 1));

        PageResponse<JobApplicationResponse> response = jobApplicationService.findAllForCurrentUser(
                new JobApplicationFilter(ApplicationStatus.INTERVIEW, "open", "backend"),
                pageable
        );

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().getFirst().companyName()).isEqualTo("OpenAI");
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(1);
    }

    @Test
    void findAllForCurrentUserRejectsUnsupportedSortField() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("user.passwordHash"));

        assertThatThrownBy(() -> jobApplicationService.findAllForCurrentUser(
                new JobApplicationFilter(null, null, null),
                pageable
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Unsupported sort field: user.passwordHash");

        verify(authenticatedUserProvider, never()).getCurrentUser();
        verify(jobApplicationRepository, never()).findAll(anyJobApplicationSpecification(), eq(pageable));
    }

    @Test
    void createRejectsInvalidSalaryRange() {
        CreateJobApplicationRequest request = new CreateJobApplicationRequest(
                "OpenAI",
                "Backend Engineer",
                "Remote",
                new BigDecimal("120000"),
                new BigDecimal("80000"),
                "https://example.com/jobs/backend",
                ApplicationStatus.APPLIED,
                LocalDate.of(2026, 5, 28),
                null
        );

        assertThatThrownBy(() -> jobApplicationService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Minimum salary cannot be greater than maximum salary");

        verify(authenticatedUserProvider, never()).getCurrentUser();
        verify(jobApplicationRepository, never()).save(any(JobApplication.class));
    }

    private Specification<JobApplication> anyJobApplicationSpecification() {
        return any();
    }
}
