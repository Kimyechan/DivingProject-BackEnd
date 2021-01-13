package com.diving.pungdong.repo;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.InstructorImage;
import com.diving.pungdong.domain.account.InstructorImgCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class InstructorImageJpaRepoTest {

    @Autowired
    private InstructorImageJpaRepo instructorImageJpaRepo;

    @Test
    @DisplayName("강사 이미지 저장")
    public void save() {
        InstructorImage image = InstructorImage.builder()
                .fileURL("test URL")
                .category(InstructorImgCategory.PROFILE)
                .instructor(new Account())
                .build();

        InstructorImage savedImage = instructorImageJpaRepo.save(image);

        assertThat(savedImage.getId()).isNotNull();
        assertThat(savedImage.getFileURL()).isEqualTo(image.getFileURL());
    }
}