package com.moustafa.jobtrackr.document;

import com.moustafa.jobtrackr.application.JobApplication;
import com.moustafa.jobtrackr.application.JobApplicationRepository;
import com.moustafa.jobtrackr.common.exception.ResourceNotFoundException;
import com.moustafa.jobtrackr.document.dto.CreateJobDocumentRequest;
import com.moustafa.jobtrackr.document.dto.JobDocumentResponse;
import com.moustafa.jobtrackr.security.AuthenticatedUserProvider;
import com.moustafa.jobtrackr.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobDocumentService {

    private final JobDocumentRepository jobDocumentRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional(readOnly = true)
    public List<JobDocumentResponse> findAllForApplication(Long applicationId) {
        User user = authenticatedUserProvider.getCurrentUser();
        ensureOwnedApplicationExists(applicationId, user);

        return jobDocumentRepository.findAllByApplication_IdAndApplication_UserOrderByCreatedAtDesc(applicationId, user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public JobDocumentResponse create(Long applicationId, CreateJobDocumentRequest request) {
        User user = authenticatedUserProvider.getCurrentUser();
        JobApplication application = getOwnedApplication(applicationId, user);

        JobDocument document = JobDocument.builder()
                .application(application)
                .name(request.name())
                .type(request.type() == null ? DocumentType.OTHER : request.type())
                .url(request.url())
                .notes(request.notes())
                .build();

        return toResponse(jobDocumentRepository.save(document));
    }

    @Transactional
    public void delete(Long id) {
        jobDocumentRepository.delete(getOwnedDocument(id));
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

    private JobDocument getOwnedDocument(Long id) {
        User user = authenticatedUserProvider.getCurrentUser();
        return jobDocumentRepository.findByIdAndApplication_User(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
    }

    private JobDocumentResponse toResponse(JobDocument document) {
        return new JobDocumentResponse(
                document.getId(),
                document.getApplication().getId(),
                document.getName(),
                document.getType(),
                document.getUrl(),
                document.getNotes(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}
