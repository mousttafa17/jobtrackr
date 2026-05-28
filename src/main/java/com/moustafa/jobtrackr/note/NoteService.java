package com.moustafa.jobtrackr.note;

import com.moustafa.jobtrackr.application.JobApplication;
import com.moustafa.jobtrackr.application.JobApplicationRepository;
import com.moustafa.jobtrackr.common.exception.ResourceNotFoundException;
import com.moustafa.jobtrackr.note.dto.CreateNoteRequest;
import com.moustafa.jobtrackr.note.dto.NoteResponse;
import com.moustafa.jobtrackr.security.AuthenticatedUserProvider;
import com.moustafa.jobtrackr.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional(readOnly = true)
    public List<NoteResponse> findAllForApplication(Long applicationId) {
        User user = authenticatedUserProvider.getCurrentUser();
        ensureOwnedApplicationExists(applicationId, user);

        return noteRepository.findAllByApplication_IdAndApplication_UserOrderByCreatedAtDesc(applicationId, user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NoteResponse create(Long applicationId, CreateNoteRequest request) {
        User user = authenticatedUserProvider.getCurrentUser();
        JobApplication application = getOwnedApplication(applicationId, user);

        Note note = Note.builder()
                .application(application)
                .content(request.content())
                .build();

        return toResponse(noteRepository.save(note));
    }

    @Transactional
    public void delete(Long id) {
        noteRepository.delete(getOwnedNote(id));
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

    private Note getOwnedNote(Long id) {
        User user = authenticatedUserProvider.getCurrentUser();
        return noteRepository.findByIdAndApplication_User(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));
    }

    private NoteResponse toResponse(Note note) {
        return new NoteResponse(
                note.getId(),
                note.getApplication().getId(),
                note.getContent(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}
