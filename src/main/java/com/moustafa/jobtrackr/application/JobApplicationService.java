package com.moustafa.jobtrackr.application;

import com.moustafa.jobtrackr.application.dto.CreateJobApplicationRequest;
import com.moustafa.jobtrackr.application.dto.JobApplicationFilter;
import com.moustafa.jobtrackr.application.dto.JobApplicationResponse;
import com.moustafa.jobtrackr.application.dto.JobApplicationStatusHistoryResponse;
import com.moustafa.jobtrackr.application.dto.UpdateApplicationStatusRequest;
import com.moustafa.jobtrackr.application.dto.UpdateJobApplicationRequest;
import com.moustafa.jobtrackr.common.exception.BadRequestException;
import com.moustafa.jobtrackr.common.exception.ResourceNotFoundException;
import com.moustafa.jobtrackr.common.response.PageResponse;
import com.moustafa.jobtrackr.security.AuthenticatedUserProvider;
import com.moustafa.jobtrackr.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt",
            "updatedAt",
            "applicationDate",
            "companyName",
            "jobTitle",
            "status"
    );

    private final JobApplicationRepository jobApplicationRepository;
    private final JobApplicationStatusHistoryRepository statusHistoryRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public PageResponse<JobApplicationResponse> findAllForCurrentUser(
            JobApplicationFilter filter,
            Pageable pageable
    ) {
        validateSort(pageable);

        User user = authenticatedUserProvider.getCurrentUser();
        Specification<JobApplication> specification = Specification.allOf(
                JobApplicationSpecifications.belongsTo(user),
                JobApplicationSpecifications.hasStatus(filter.status()),
                JobApplicationSpecifications.companyContains(filter.company()),
                JobApplicationSpecifications.searchContains(filter.search())
        );

        return PageResponse.from(jobApplicationRepository.findAll(specification, pageable)
                .map(this::toResponse));
    }

    @Transactional
    public JobApplicationResponse findByIdForCurrentUser(Long id) {
        return toResponse(getOwnedApplication(id));
    }

    @Transactional(readOnly = true)
    public List<JobApplicationStatusHistoryResponse> findStatusHistory(Long id) {
        User user = authenticatedUserProvider.getCurrentUser();
        if (jobApplicationRepository.findByIdAndUser(id, user).isEmpty()) {
            throw new ResourceNotFoundException("Job application not found");
        }

        return statusHistoryRepository.findAllByApplication_IdAndApplication_UserOrderByChangedAtDesc(id, user)
                .stream()
                .map(this::toStatusHistoryResponse)
                .toList();
    }

    @Transactional
    public JobApplicationResponse create(CreateJobApplicationRequest request) {
        validateSalaryRange(request.salaryMin(), request.salaryMax());

        ApplicationStatus status = request.status() == null ? ApplicationStatus.APPLIED : request.status();
        JobApplication application = JobApplication.builder()
                .user(authenticatedUserProvider.getCurrentUser())
                .companyName(request.companyName())
                .jobTitle(request.jobTitle())
                .location(request.location())
                .salaryMin(request.salaryMin())
                .salaryMax(request.salaryMax())
                .jobLink(request.jobLink())
                .status(status)
                .applicationDate(request.applicationDate())
                .notes(request.notes())
                .build();

        JobApplication savedApplication = jobApplicationRepository.save(application);
        recordStatusChange(savedApplication, null, status);
        return toResponse(savedApplication);
    }

    @Transactional
    public JobApplicationResponse update(Long id, UpdateJobApplicationRequest request) {
        validateSalaryRange(request.salaryMin(), request.salaryMax());

        JobApplication application = getOwnedApplication(id);
        ApplicationStatus oldStatus = application.getStatus();
        ApplicationStatus newStatus = request.status() == null ? ApplicationStatus.APPLIED : request.status();

        application.setCompanyName(request.companyName());
        application.setJobTitle(request.jobTitle());
        application.setLocation(request.location());
        application.setSalaryMin(request.salaryMin());
        application.setSalaryMax(request.salaryMax());
        application.setJobLink(request.jobLink());
        application.setStatus(newStatus);
        application.setApplicationDate(request.applicationDate());
        application.setNotes(request.notes());

        JobApplication savedApplication = jobApplicationRepository.saveAndFlush(application);
        recordStatusChangeIfChanged(savedApplication, oldStatus, newStatus);
        return toResponse(savedApplication);
    }

    @Transactional
    public JobApplicationResponse updateStatus(Long id, UpdateApplicationStatusRequest request) {
        JobApplication application = getOwnedApplication(id);
        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(request.status());
        JobApplication savedApplication = jobApplicationRepository.saveAndFlush(application);
        recordStatusChangeIfChanged(savedApplication, oldStatus, request.status());
        return toResponse(savedApplication);
    }

    @Transactional
    public void delete(Long id) {
        jobApplicationRepository.delete(getOwnedApplication(id));
    }

    private JobApplication getOwnedApplication(Long id) {
        User user = authenticatedUserProvider.getCurrentUser();
        return jobApplicationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Job application not found"));
    }

    private void validateSalaryRange(BigDecimal salaryMin, BigDecimal salaryMax) {
        if (salaryMin != null && salaryMax != null && salaryMin.compareTo(salaryMax) > 0) {
            throw new BadRequestException("Minimum salary cannot be greater than maximum salary");
        }
    }

    private void validateSort(Pageable pageable) {
        pageable.getSort().forEach(order -> {
            if (!ALLOWED_SORT_FIELDS.contains(order.getProperty())) {
                throw new BadRequestException("Unsupported sort field: " + order.getProperty());
            }
        });
    }

    private void recordStatusChangeIfChanged(
            JobApplication application,
            ApplicationStatus oldStatus,
            ApplicationStatus newStatus
    ) {
        if (oldStatus != newStatus) {
            recordStatusChange(application, oldStatus, newStatus);
        }
    }

    private void recordStatusChange(
            JobApplication application,
            ApplicationStatus oldStatus,
            ApplicationStatus newStatus
    ) {
        statusHistoryRepository.save(JobApplicationStatusHistory.builder()
                .application(application)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .build());
    }

    private JobApplicationResponse toResponse(JobApplication application) {
        return new JobApplicationResponse(
                application.getId(),
                application.getUser().getId(),
                application.getCompanyName(),
                application.getJobTitle(),
                application.getLocation(),
                application.getSalaryMin(),
                application.getSalaryMax(),
                application.getJobLink(),
                application.getStatus(),
                application.getApplicationDate(),
                application.getNotes(),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }

    private JobApplicationStatusHistoryResponse toStatusHistoryResponse(JobApplicationStatusHistory history) {
        return new JobApplicationStatusHistoryResponse(
                history.getId(),
                history.getApplication().getId(),
                history.getOldStatus(),
                history.getNewStatus(),
                history.getChangedAt()
        );
    }
}
