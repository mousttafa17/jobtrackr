package com.moustafa.jobtrackr.application;

import com.moustafa.jobtrackr.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findByUserOrderByCreatedAtDesc(User user);

    Optional<JobApplication> findByIdAndUser(Long id, User user);
}
