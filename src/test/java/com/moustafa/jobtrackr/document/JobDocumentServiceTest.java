package com.moustafa.jobtrackr.document;

import com.moustafa.jobtrackr.application.ApplicationStatus;
import com.moustafa.jobtrackr.application.JobApplication;
import com.moustafa.jobtrackr.application.JobApplicationRepository;
import com.moustafa.jobtrackr.common.exception.ResourceNotFoundException;
import com.moustafa.jobtrackr.document.dto.CreateJobDocumentRequest;
import com.moustafa.jobtrackr.document.dto.JobDocumentResponse;
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
class JobDocumentServiceTest {

    @Mock
    private JobDocumentRepository jobDocumentRepository;

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    private JobDocumentService jobDocumentService;
    private User user;
    private JobApplication application;

    @BeforeEach
    void setUp() {
        jobDocumentService = new JobDocumentService(
                jobDocumentRepository,
                jobApplicationRepository,
                authenticatedUserProvider
        );
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
    void findAllForApplicationReturnsOwnedDocuments() {
        JobDocument document = JobDocument.builder()
                .id(20L)
                .application(application)
                .name("OpenAI resume")
                .type(DocumentType.RESUME)
                .url("https://example.com/resume.pdf")
                .notes("Tailored resume")
                .createdAt(Instant.parse("2026-05-28T05:30:00Z"))
                .updatedAt(Instant.parse("2026-05-28T05:30:00Z"))
                .build();

        when(authenticatedUserProvider.getCurrentUser()).thenReturn(user);
        when(jobApplicationRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(application));
        when(jobDocumentRepository.findAllByApplication_IdAndApplication_UserOrderByCreatedAtDesc(10L, user))
                .thenReturn(List.of(document));

        List<JobDocumentResponse> response = jobDocumentService.findAllForApplication(10L);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().id()).isEqualTo(20L);
        assertThat(response.getFirst().applicationId()).isEqualTo(10L);
        assertThat(response.getFirst().type()).isEqualTo(DocumentType.RESUME);
        assertThat(response.getFirst().url()).isEqualTo("https://example.com/resume.pdf");
    }

    @Test
    void createStoresDocumentForOwnedApplication() {
        CreateJobDocumentRequest request = new CreateJobDocumentRequest(
                "OpenAI cover letter",
                DocumentType.COVER_LETTER,
                "https://example.com/cover-letter.pdf",
                "Short version"
        );

        when(authenticatedUserProvider.getCurrentUser()).thenReturn(user);
        when(jobApplicationRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(application));
        when(jobDocumentRepository.save(any(JobDocument.class))).thenAnswer(invocation -> {
            JobDocument saved = invocation.getArgument(0);
            saved.setId(30L);
            saved.setCreatedAt(Instant.parse("2026-05-28T06:00:00Z"));
            saved.setUpdatedAt(Instant.parse("2026-05-28T06:00:00Z"));
            return saved;
        });

        JobDocumentResponse response = jobDocumentService.create(10L, request);

        assertThat(response.id()).isEqualTo(30L);
        assertThat(response.applicationId()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("OpenAI cover letter");
        assertThat(response.type()).isEqualTo(DocumentType.COVER_LETTER);

        ArgumentCaptor<JobDocument> documentCaptor = ArgumentCaptor.forClass(JobDocument.class);
        verify(jobDocumentRepository).save(documentCaptor.capture());
        assertThat(documentCaptor.getValue().getApplication()).isEqualTo(application);
        assertThat(documentCaptor.getValue().getUrl()).isEqualTo("https://example.com/cover-letter.pdf");
    }

    @Test
    void createDefaultsMissingTypeToOther() {
        CreateJobDocumentRequest request = new CreateJobDocumentRequest(
                "Misc doc",
                null,
                "https://example.com/doc.pdf",
                null
        );

        when(authenticatedUserProvider.getCurrentUser()).thenReturn(user);
        when(jobApplicationRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(application));
        when(jobDocumentRepository.save(any(JobDocument.class))).thenAnswer(invocation -> {
            JobDocument saved = invocation.getArgument(0);
            saved.setId(31L);
            saved.setCreatedAt(Instant.parse("2026-05-28T06:00:00Z"));
            saved.setUpdatedAt(Instant.parse("2026-05-28T06:00:00Z"));
            return saved;
        });

        JobDocumentResponse response = jobDocumentService.create(10L, request);

        assertThat(response.type()).isEqualTo(DocumentType.OTHER);
    }

    @Test
    void createRejectsDocumentForApplicationNotOwnedByCurrentUser() {
        CreateJobDocumentRequest request = new CreateJobDocumentRequest(
                "Resume",
                DocumentType.RESUME,
                "https://example.com/resume.pdf",
                null
        );

        when(authenticatedUserProvider.getCurrentUser()).thenReturn(user);
        when(jobApplicationRepository.findByIdAndUser(999L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobDocumentService.create(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Job application not found");

        verify(jobDocumentRepository, never()).save(any(JobDocument.class));
    }

    @Test
    void deleteRemovesOwnedDocument() {
        JobDocument document = JobDocument.builder()
                .id(40L)
                .application(application)
                .name("Temporary document")
                .type(DocumentType.OTHER)
                .url("https://example.com/temp.pdf")
                .createdAt(Instant.parse("2026-05-28T06:00:00Z"))
                .updatedAt(Instant.parse("2026-05-28T06:00:00Z"))
                .build();

        when(authenticatedUserProvider.getCurrentUser()).thenReturn(user);
        when(jobDocumentRepository.findByIdAndApplication_User(40L, user)).thenReturn(Optional.of(document));

        jobDocumentService.delete(40L);

        verify(jobDocumentRepository).delete(document);
    }

    @Test
    void deleteRejectsDocumentNotOwnedByCurrentUser() {
        when(authenticatedUserProvider.getCurrentUser()).thenReturn(user);
        when(jobDocumentRepository.findByIdAndApplication_User(404L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobDocumentService.delete(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Document not found");

        verify(jobDocumentRepository, never()).delete(any(JobDocument.class));
    }
}
