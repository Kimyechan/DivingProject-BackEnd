package com.diving.pungdong.repo;

import com.diving.pungdong.domain.account.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentJpaRepo extends JpaRepository<Student, Long> {
}
