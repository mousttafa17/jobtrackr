package com.moustafa.jobtrackr.note;

import com.moustafa.jobtrackr.note.dto.CreateNoteRequest;
import com.moustafa.jobtrackr.note.dto.NoteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Notes", description = "Manage notes for job applications")
public class NoteController {

    private final NoteService noteService;

    @Operation(summary = "List notes for a job application")
    @GetMapping("/api/applications/{applicationId}/notes")
    public List<NoteResponse> findAllForApplication(@PathVariable Long applicationId) {
        return noteService.findAllForApplication(applicationId);
    }

    @Operation(summary = "Create a note for a job application")
    @PostMapping("/api/applications/{applicationId}/notes")
    @ResponseStatus(HttpStatus.CREATED)
    public NoteResponse create(
            @PathVariable Long applicationId,
            @Valid @RequestBody CreateNoteRequest request
    ) {
        return noteService.create(applicationId, request);
    }

    @Operation(summary = "Delete a note")
    @DeleteMapping("/api/notes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        noteService.delete(id);
    }
}
