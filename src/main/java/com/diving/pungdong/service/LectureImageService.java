package com.diving.pungdong.service;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.LectureImage;
import com.diving.pungdong.dto.lectureImage.LectureImageInfo;
import com.diving.pungdong.dto.lectureImage.LectureImageUrl;
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

    @Transactional
    public LectureImageInfo saveImages(Long lectureId, Account account, List<MultipartFile> images) throws IOException {
        lectureService.checkLectureCreator(account, lectureId);
        Lecture lecture = lectureService.findLectureById(lectureId);

        List<String> imageUris = new ArrayList<>();
        for (MultipartFile image : images) {
            String fileUri = s3Uploader.upload(image, "lecture-image", account.getEmail());
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

    @Transactional(readOnly = true)
    public List<LectureImageUrl> findLectureImagesUrl(Long lectureId) {
        List<LectureImage> lectureImages = lectureImageJpaRepo.findAllByLectureId(lectureId);

        List<LectureImageUrl> lectureImageUrls = new ArrayList<>();
        for (LectureImage lectureImage : lectureImages) {
            LectureImageUrl url = LectureImageUrl.builder()
                    .lectureImageId(lectureImage.getId())
                    .url(lectureImage.getFileURI())
                    .build();
            lectureImageUrls.add(url);
        }

        return lectureImageUrls;
    }
}
