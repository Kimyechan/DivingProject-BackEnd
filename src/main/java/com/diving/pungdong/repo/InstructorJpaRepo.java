package com.diving.pungdong.repo;

import com.diving.pungdong.domain.account.instructor.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstructorJpaRepo extends JpaRepository<Instructor, Long> {
    Optional<Instructor> findByEmail(String email);
}
