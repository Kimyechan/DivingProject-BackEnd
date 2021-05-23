package com.diving.pungdong.service;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.LectureImage;
import com.diving.pungdong.dto.lecture.update.LectureImageUpdate;
import com.diving.pungdong.dto.lecture.update.LectureUpdateInfo;
import com.diving.pungdong.dto.lectureImage.LectureImageInfo;
import com.diving.pungdong.repo.LectureImageJpaRepo;
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
public class LectureImageService {
    private final LectureImageJpaRepo lectureImageJpaRepo;
    private final LectureService lectureService;
    private final S3Uploader s3Uploader;

    public LectureImage saveLectureImage(LectureImage lectureImage) {
        return lectureImageJpaRepo.save(lectureImage);
    }

    public void deleteByURL(String lectureImageURL) {
        lectureImageJpaRepo.deleteByFileURI(lectureImageURL);
    }

    public void deleteIfIsDeleted(LectureUpdateInfo lectureUpdateInfo) {
        if (!lectureUpdateInfo.getLectureImageUpdateList().isEmpty()) {
            for (LectureImageUpdate lectureImageUpdate : lectureUpdateInfo.getLectureImageUpdateList()) {
                if (lectureImageUpdate.getIsDeleted()) {
                    lectureImageJpaRepo.deleteByFileURI(lectureImageUpdate.getLectureImageURL());
                    s3Uploader.deleteFileFromS3(lectureImageUpdate.getLectureImageURL());
                }
            }
        }
    }

    public void addList(String email, List<MultipartFile> addLectureImageFiles, Lecture lecture) throws IOException {
        if (!addLectureImageFiles.isEmpty()) {
            for (MultipartFile file : addLectureImageFiles) {
                String fileURI = s3Uploader.upload(file, "lecture", email);
                LectureImage lectureImage = LectureImage.builder()
                        .fileURI(fileURI)
                        .lecture(lecture)
                        .build();
                lectureImageJpaRepo.save(lectureImage);
            }
        }
    }

    @Transactional
    public LectureImageInfo saveImages(Long lectureId, Account account, List<MultipartFile> images) throws IOException {
        lectureService.checkLectureCreator(account, lectureId);
        Lecture lecture = lectureService.getLectureById(lectureId);

        List<String> imageUris = new ArrayList<>();
        for (MultipartFile image : images) {
            String fileUri = s3Uploader.upload(image, "lecture", account.getEmail());
            LectureImage lectureImage = LectureImage.builder()
                    .fileURI(fileUri)
                    .lecture(lecture)
                    .build();

            LectureImage savedLectureImage = lectureImageJpaRepo.save(lectureImage);
            imageUris.add(savedLectureImage.getFileURI());
        }

        return LectureImageInfo.builder()
                .lectureId(lecture.getId())
                .imageUris(imageUris)
                .build();
    }
}
