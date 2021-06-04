package com.diving.pungdong.repo;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.InstructorCertificate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class InstructorCertificateJpaRepoTest {

    @Autowired
    private InstructorCertificateJpaRepo instructorCertificateJpaRepo;

    @Test
    @DisplayName("강사 이미지 저장")
    public void save() {
        InstructorCertificate image = InstructorCertificate.builder()
                .fileURL("test URL")
                .instructor(new Account())
                .build();

        InstructorCertificate savedImage = instructorCertificateJpaRepo.save(image);

        assertThat(savedImage.getId()).isNotNull();
        assertThat(savedImage.getFileURL()).isEqualTo(image.getFileURL());
    }
}