package com.moustafa.jobtrackr.document;

import com.moustafa.jobtrackr.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobDocumentRepository extends JpaRepository<JobDocument, Long> {

    List<JobDocument> findAllByApplication_IdAndApplication_UserOrderByCreatedAtDesc(Long applicationId, User user);

    Optional<JobDocument> findByIdAndApplication_User(Long id, User user);
}
