package com.diving.pungdong.repo;

import com.diving.pungdong.domain.account.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstructorJpaRepo extends JpaRepository<Instructor, Long> {
}
