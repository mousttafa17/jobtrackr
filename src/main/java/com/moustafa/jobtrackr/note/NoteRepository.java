package com.moustafa.jobtrackr.note;

import com.moustafa.jobtrackr.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findAllByApplication_IdAndApplication_UserOrderByCreatedAtDesc(Long applicationId, User user);

    Optional<Note> findByIdAndApplication_User(Long id, User user);
}
