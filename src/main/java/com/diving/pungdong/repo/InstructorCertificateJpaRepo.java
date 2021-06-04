package com.diving.pungdong.repo;

import com.diving.pungdong.domain.account.InstructorCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstructorCertificateJpaRepo extends JpaRepository<InstructorCertificate, Long> {
}
