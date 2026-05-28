package com.moustafa.jobtrackr.application;

import com.moustafa.jobtrackr.application.dto.CreateJobApplicationRequest;
import com.moustafa.jobtrackr.application.dto.JobApplicationResponse;
import com.moustafa.jobtrackr.application.dto.UpdateApplicationStatusRequest;
import com.moustafa.jobtrackr.application.dto.UpdateJobApplicationRequest;
import com.moustafa.jobtrackr.common.exception.BadRequestException;
import com.moustafa.jobtrackr.common.exception.ResourceNotFoundException;
import com.moustafa.jobtrackr.user.DefaultUserProvider;
import com.moustafa.jobtrackr.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final DefaultUserProvider defaultUserProvider;

    @Transactional
    public List<JobApplicationResponse> findAllForCurrentUser() {
        User user = defaultUserProvider.getCurrentUser();
        return jobApplicationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public JobApplicationResponse findByIdForCurrentUser(Long id) {
        return toResponse(getOwnedApplication(id));
    }

    @Transactional
    public JobApplicationResponse create(CreateJobApplicationRequest request) {
        validateSalaryRange(request.salaryMin(), request.salaryMax());

        JobApplication application = JobApplication.builder()
                .user(defaultUserProvider.getCurrentUser())
                .companyName(request.companyName())
                .jobTitle(request.jobTitle())
                .location(request.location())
                .salaryMin(request.salaryMin())
                .salaryMax(request.salaryMax())
                .jobLink(request.jobLink())
                .status(request.status())
                .applicationDate(request.applicationDate())
                .notes(request.notes())
                .build();

        return toResponse(jobApplicationRepository.save(application));
    }

    @Transactional
    public JobApplicationResponse update(Long id, UpdateJobApplicationRequest request) {
        validateSalaryRange(request.salaryMin(), request.salaryMax());

        JobApplication application = getOwnedApplication(id);
        application.setCompanyName(request.companyName());
        application.setJobTitle(request.jobTitle());
        application.setLocation(request.location());
        application.setSalaryMin(request.salaryMin());
        application.setSalaryMax(request.salaryMax());
        application.setJobLink(request.jobLink());
        application.setStatus(request.status() == null ? ApplicationStatus.APPLIED : request.status());
        application.setApplicationDate(request.applicationDate());
        application.setNotes(request.notes());

        return toResponse(jobApplicationRepository.saveAndFlush(application));
    }

    @Transactional
    public JobApplicationResponse updateStatus(Long id, UpdateApplicationStatusRequest request) {
        JobApplication application = getOwnedApplication(id);
        application.setStatus(request.status());
        return toResponse(jobApplicationRepository.saveAndFlush(application));
    }

    @Transactional
    public void delete(Long id) {
        jobApplicationRepository.delete(getOwnedApplication(id));
    }

    private JobApplication getOwnedApplication(Long id) {
        User user = defaultUserProvider.getCurrentUser();
        return jobApplicationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Job application not found"));
    }

    private void validateSalaryRange(BigDecimal salaryMin, BigDecimal salaryMax) {
        if (salaryMin != null && salaryMax != null && salaryMin.compareTo(salaryMax) > 0) {
            throw new BadRequestException("Minimum salary cannot be greater than maximum salary");
        }
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
}
