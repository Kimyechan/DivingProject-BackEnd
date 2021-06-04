package com.diving.pungdong.service;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.InstructorImgCategory;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.repo.InstructorCertificateJpaRepo;
import com.diving.pungdong.service.image.S3Uploader;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class InstructorCertificateServiceTest {
    @InjectMocks
    private InstructorCertificateService instructorCertificateService;

    @Mock
    private InstructorCertificateJpaRepo instructorCertificateJpaRepo;

    @Mock
    private S3Uploader s3Uploader;


    @Test
    @DisplayName("강사 관련 여러장의 이미지 저장")
    public void uploadInstructorImages() throws IOException {
        Account updateAccount = Account.builder()
                .id(1L)
                .email("rrr@gmail.com")
                .password("1234")
                .nickName("yechan")
                .birth("1999-09-11")
                .gender(Gender.MALE)
                .roles(Sets.newLinkedHashSet(Role.STUDENT, Role.INSTRUCTOR))
                .build();

        MockMultipartFile file1 = new MockMultipartFile("profile", "file1.png", "image/*", "test data".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("profile", "file2.png", "image/*", "test data".getBytes());
        given(s3Uploader.upload(file1, "test", updateAccount.getEmail())).willReturn("test URL 1");
        given(s3Uploader.upload(file2, "test", updateAccount.getEmail())).willReturn("test URL 2");

        instructorCertificateService.uploadInstructorImages(updateAccount.getEmail(), List.of(file1, file2), updateAccount, "test", InstructorImgCategory.PROFILE);

        verify(s3Uploader, times(1)).upload(file1, "test", updateAccount.getEmail());
        verify(s3Uploader, times(1)).upload(file2, "test", updateAccount.getEmail());
        verify(instructorCertificateJpaRepo, times(2)).save(any());
    }

}