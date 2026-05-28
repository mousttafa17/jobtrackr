package com.moustafa.jobtrackr.interview;

import com.moustafa.jobtrackr.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findAllByApplication_IdAndApplication_UserOrderByScheduledAtAsc(Long applicationId, User user);

    Optional<Interview> findByIdAndApplication_User(Long id, User user);
}
