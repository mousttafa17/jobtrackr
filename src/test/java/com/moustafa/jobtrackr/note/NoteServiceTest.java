package com.moustafa.jobtrackr.note;

import com.moustafa.jobtrackr.application.ApplicationStatus;
import com.moustafa.jobtrackr.application.JobApplication;
import com.moustafa.jobtrackr.application.JobApplicationRepository;
import com.moustafa.jobtrackr.common.exception.ResourceNotFoundException;
import com.moustafa.jobtrackr.note.dto.CreateNoteRequest;
import com.moustafa.jobtrackr.note.dto.NoteResponse;
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
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    private NoteService noteService;
    private User user;
    private JobApplication application;

    @BeforeEach
    void setUp() {
        noteService = new NoteService(noteRepository, jobApplicationRepository, authenticatedUserProvider);
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
    void findAllForApplicationReturnsOwnedNotes() {
        Note note = Note.builder()
                .id(20L)
                .application(application)
                .content("Recruiter mentioned a follow-up next week.")
                .createdAt(Instant.parse("2026-05-28T05:30:00Z"))
                .updatedAt(Instant.parse("2026-05-28T05:30:00Z"))
                .build();

        when(authenticatedUserProvider.getCurrentUser()).thenReturn(user);
        when(jobApplicationRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(application));
        when(noteRepository.findAllByApplication_IdAndApplication_UserOrderByCreatedAtDesc(10L, user))
                .thenReturn(List.of(note));

        List<NoteResponse> response = noteService.findAllForApplication(10L);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().id()).isEqualTo(20L);
        assertThat(response.getFirst().applicationId()).isEqualTo(10L);
        assertThat(response.getFirst().content()).isEqualTo("Recruiter mentioned a follow-up next week.");
    }

    @Test
    void createStoresNoteForOwnedApplication() {
        CreateNoteRequest request = new CreateNoteRequest("Ask about team ownership model.");

        when(authenticatedUserProvider.getCurrentUser()).thenReturn(user);
        when(jobApplicationRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(application));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> {
            Note saved = invocation.getArgument(0);
            saved.setId(30L);
            saved.setCreatedAt(Instant.parse("2026-05-28T06:00:00Z"));
            saved.setUpdatedAt(Instant.parse("2026-05-28T06:00:00Z"));
            return saved;
        });

        NoteResponse response = noteService.create(10L, request);

        assertThat(response.id()).isEqualTo(30L);
        assertThat(response.applicationId()).isEqualTo(10L);
        assertThat(response.content()).isEqualTo("Ask about team ownership model.");

        ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(noteCaptor.capture());
        assertThat(noteCaptor.getValue().getApplication()).isEqualTo(application);
        assertThat(noteCaptor.getValue().getContent()).isEqualTo("Ask about team ownership model.");
    }

    @Test
    void createRejectsNoteForApplicationNotOwnedByCurrentUser() {
        CreateNoteRequest request = new CreateNoteRequest("Follow up later.");

        when(authenticatedUserProvider.getCurrentUser()).thenReturn(user);
        when(jobApplicationRepository.findByIdAndUser(999L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.create(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Job application not found");

        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void deleteRemovesOwnedNote() {
        Note note = Note.builder()
                .id(40L)
                .application(application)
                .content("Temporary note")
                .createdAt(Instant.parse("2026-05-28T06:00:00Z"))
                .updatedAt(Instant.parse("2026-05-28T06:00:00Z"))
                .build();

        when(authenticatedUserProvider.getCurrentUser()).thenReturn(user);
        when(noteRepository.findByIdAndApplication_User(40L, user)).thenReturn(Optional.of(note));

        noteService.delete(40L);

        verify(noteRepository).delete(note);
    }

    @Test
    void deleteRejectsNoteNotOwnedByCurrentUser() {
        when(authenticatedUserProvider.getCurrentUser()).thenReturn(user);
        when(noteRepository.findByIdAndApplication_User(404L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.delete(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Note not found");

        verify(noteRepository, never()).delete(any(Note.class));
    }
}
