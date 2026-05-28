package com.moustafa.jobtrackr.task;

import com.moustafa.jobtrackr.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobTaskRepository extends JpaRepository<JobTask, Long> {

    List<JobTask> findAllByApplication_IdAndApplication_UserOrderByCompletedAscDueAtAsc(
            Long applicationId,
            User user
    );

    Optional<JobTask> findByIdAndApplication_User(Long id, User user);
}
