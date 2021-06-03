package com.diving.pungdong.service;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.InstructorCertificate;
import com.diving.pungdong.domain.account.InstructorImgCategory;
import com.diving.pungdong.repo.InstructorImageJpaRepo;
import com.diving.pungdong.service.image.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InstructorImageService {

    private final InstructorImageJpaRepo instructorImageJpaRepo;
    private final S3Uploader s3Uploader;

    public InstructorCertificate saveInstructorImage(InstructorCertificate image) {
        return instructorImageJpaRepo.save(image);
    }

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

            instructorImageJpaRepo.save(image);
        }
    }
}
