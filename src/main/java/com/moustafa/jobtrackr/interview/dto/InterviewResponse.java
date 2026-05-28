package com.moustafa.jobtrackr.interview.dto;

import com.moustafa.jobtrackr.interview.InterviewStatus;
import com.moustafa.jobtrackr.interview.InterviewType;

import java.time.Instant;

public record InterviewResponse(
        Long id,
        Long applicationId,
        InterviewType type,
        InterviewStatus status,
        Instant scheduledAt,
        String location,
        String meetingLink,
        String interviewerName,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
