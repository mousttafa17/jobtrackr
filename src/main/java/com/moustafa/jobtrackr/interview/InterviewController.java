package com.moustafa.jobtrackr.interview;

import com.moustafa.jobtrackr.interview.dto.CreateInterviewRequest;
import com.moustafa.jobtrackr.interview.dto.InterviewResponse;
import com.moustafa.jobtrackr.interview.dto.UpdateInterviewRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @GetMapping("/api/applications/{applicationId}/interviews")
    public List<InterviewResponse> findAllForApplication(@PathVariable Long applicationId) {
        return interviewService.findAllForApplication(applicationId);
    }

    @PostMapping("/api/applications/{applicationId}/interviews")
    @ResponseStatus(HttpStatus.CREATED)
    public InterviewResponse create(
            @PathVariable Long applicationId,
            @Valid @RequestBody CreateInterviewRequest request
    ) {
        return interviewService.create(applicationId, request);
    }

    @PutMapping("/api/interviews/{id}")
    public InterviewResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInterviewRequest request
    ) {
        return interviewService.update(id, request);
    }

    @DeleteMapping("/api/interviews/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        interviewService.delete(id);
    }
}
