package com.diving.pungdong.service;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.InstructorCertificate;
import com.diving.pungdong.domain.account.InstructorImgCategory;
import com.diving.pungdong.dto.account.instructor.certificate.InstructorCertificateInfo;
import com.diving.pungdong.model.SuccessResult;
import com.diving.pungdong.repo.InstructorCertificateJpaRepo;
import com.diving.pungdong.service.account.AccountService;
import com.diving.pungdong.service.image.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InstructorCertificateService {
    private final AccountService accountService;
    private final InstructorCertificateJpaRepo instructorCertificateJpaRepo;
    private final S3Uploader s3Uploader;

    public void uploadInstructorImages(String email,
                                       List<MultipartFile> files,
                                       Account updateAccount,
                                       String dirName,
                                       InstructorImgCategory instructorImgCategory) throws IOException {
        for (MultipartFile file : files) {
            String fileURL = s3Uploader.upload(file, dirName, email);
            InstructorCertificate image = InstructorCertificate.builder()
                    .fileURL(fileURL)
                    .instructor(updateAccount)
                    .build();

            instructorCertificateJpaRepo.save(image);
        }
    }

    @Transactional
    public SuccessResult saveInstructorCertificate(Account account,
                                                   List<MultipartFile> certificateImages) throws IOException {
        for (MultipartFile certificateImage : certificateImages) {
            String fileURL = s3Uploader.upload(certificateImage, "instructorCertificate", account.getEmail());
            InstructorCertificate image = InstructorCertificate.builder()
                    .fileURL(fileURL)
                    .instructor(account)
                    .build();

            instructorCertificateJpaRepo.save(image);
        }

        accountService.updateIsRequestCertificated(account);

        return SuccessResult.builder()
                .success(true)
                .build();
    }

    public List<InstructorCertificate> findInstructorCertificates(Account account) {
        return instructorCertificateJpaRepo.findByInstructor(account);
    }

    public List<InstructorCertificateInfo> mapToInstructorCertificateInfos(List<InstructorCertificate> instructorCertificateList) {
        List<InstructorCertificateInfo> certificateInfos = new ArrayList<>();
        for (InstructorCertificate instructorCertificate : instructorCertificateList) {
            InstructorCertificateInfo certificateInfo = InstructorCertificateInfo.builder()
                    .id(instructorCertificate.getId())
                    .imageUrl(instructorCertificate.getFileURL())
                    .build();
            certificateInfos.add(certificateInfo);
        }

        return certificateInfos;
    }
}
