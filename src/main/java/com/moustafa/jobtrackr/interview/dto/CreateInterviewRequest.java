package com.moustafa.jobtrackr.interview.dto;

import com.moustafa.jobtrackr.interview.InterviewStatus;
import com.moustafa.jobtrackr.interview.InterviewType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateInterviewRequest(
        @NotNull(message = "Interview type is required")
        InterviewType type,

        InterviewStatus status,

        @NotNull(message = "Scheduled time is required")
        Instant scheduledAt,

        @Size(max = 120, message = "Location must be at most 120 characters")
        String location,

        @Size(max = 2048, message = "Meeting link must be at most 2048 characters")
        String meetingLink,

        @Size(max = 120, message = "Interviewer name must be at most 120 characters")
        String interviewerName,

        @Size(max = 5000, message = "Notes must be at most 5000 characters")
        String notes
) {
}
