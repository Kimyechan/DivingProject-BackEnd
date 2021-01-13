package com.diving.pungdong.service;

import com.diving.pungdong.config.S3Uploader;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.InstructorImage;
import com.diving.pungdong.domain.account.InstructorImgCategory;
import com.diving.pungdong.repo.InstructorImageJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InstructorImageService {

    private final InstructorImageJpaRepo instructorImageJpaRepo;
    private final S3Uploader s3Uploader;

    public InstructorImage saveInstructorImage(InstructorImage image) {
        return instructorImageJpaRepo.save(image);
    }

    public void uploadInstructorImages(String email,
                                       List<MultipartFile> files,
                                       Account updateAccount,
                                       String dirName,
                                       InstructorImgCategory instructorImgCategory) throws IOException {
        for (MultipartFile file : files) {
            String fileURL = s3Uploader.upload(file, dirName, email);
            InstructorImage image = InstructorImage.builder()
                    .fileURL(fileURL)
                    .category(instructorImgCategory)
                    .instructor(updateAccount)
                    .build();

            saveInstructorImage(image);
            updateAccount.getInstructorImages().add(image);
        }
    }
}
