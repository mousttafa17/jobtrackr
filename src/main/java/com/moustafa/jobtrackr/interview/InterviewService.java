package com.moustafa.jobtrackr.interview;

import com.moustafa.jobtrackr.application.JobApplication;
import com.moustafa.jobtrackr.application.JobApplicationRepository;
import com.moustafa.jobtrackr.common.exception.ResourceNotFoundException;
import com.moustafa.jobtrackr.interview.dto.CreateInterviewRequest;
import com.moustafa.jobtrackr.interview.dto.InterviewResponse;
import com.moustafa.jobtrackr.interview.dto.UpdateInterviewRequest;
import com.moustafa.jobtrackr.security.AuthenticatedUserProvider;
import com.moustafa.jobtrackr.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional(readOnly = true)
    public List<InterviewResponse> findAllForApplication(Long applicationId) {
        User user = authenticatedUserProvider.getCurrentUser();
        ensureOwnedApplicationExists(applicationId, user);

        return interviewRepository.findAllByApplication_IdAndApplication_UserOrderByScheduledAtAsc(applicationId, user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public InterviewResponse create(Long applicationId, CreateInterviewRequest request) {
        User user = authenticatedUserProvider.getCurrentUser();
        JobApplication application = getOwnedApplication(applicationId, user);

        Interview interview = Interview.builder()
                .application(application)
                .type(request.type())
                .status(request.status())
                .scheduledAt(request.scheduledAt())
                .location(request.location())
                .meetingLink(request.meetingLink())
                .interviewerName(request.interviewerName())
                .notes(request.notes())
                .build();

        return toResponse(interviewRepository.save(interview));
    }

    @Transactional
    public InterviewResponse update(Long id, UpdateInterviewRequest request) {
        Interview interview = getOwnedInterview(id);
        interview.setType(request.type());
        interview.setStatus(request.status());
        interview.setScheduledAt(request.scheduledAt());
        interview.setLocation(request.location());
        interview.setMeetingLink(request.meetingLink());
        interview.setInterviewerName(request.interviewerName());
        interview.setNotes(request.notes());

        return toResponse(interviewRepository.saveAndFlush(interview));
    }

    @Transactional
    public void delete(Long id) {
        interviewRepository.delete(getOwnedInterview(id));
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

    private Interview getOwnedInterview(Long id) {
        User user = authenticatedUserProvider.getCurrentUser();
        return interviewRepository.findByIdAndApplication_User(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found"));
    }

    private InterviewResponse toResponse(Interview interview) {
        return new InterviewResponse(
                interview.getId(),
                interview.getApplication().getId(),
                interview.getType(),
                interview.getStatus(),
                interview.getScheduledAt(),
                interview.getLocation(),
                interview.getMeetingLink(),
                interview.getInterviewerName(),
                interview.getNotes(),
                interview.getCreatedAt(),
                interview.getUpdatedAt()
        );
    }
}
