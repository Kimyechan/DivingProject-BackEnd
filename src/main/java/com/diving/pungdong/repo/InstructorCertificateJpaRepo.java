package com.diving.pungdong.repo;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.InstructorCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstructorCertificateJpaRepo extends JpaRepository<InstructorCertificate, Long> {
    List<InstructorCertificate> findByInstructor(Account instructor);
}