package com.moustafa.jobtrackr.application;

import com.moustafa.jobtrackr.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobApplicationStatusHistoryRepository extends JpaRepository<JobApplicationStatusHistory, Long> {

    List<JobApplicationStatusHistory> findAllByApplication_IdAndApplication_UserOrderByChangedAtDesc(
            Long applicationId,
            User user
    );
}
