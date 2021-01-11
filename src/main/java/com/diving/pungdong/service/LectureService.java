package com.diving.pungdong.service;

import com.diving.pungdong.config.S3Uploader;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.LectureImage;
import com.diving.pungdong.repo.LectureJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LectureService {
    private final LectureJpaRepo lectureJpaRepo;
    private final LectureImageService lectureImageService;
    private final S3Uploader s3Uploader;

    public Lecture saveLecture(Lecture lecture) {
        return lectureJpaRepo.save(lecture);
    }

    public Page<Lecture> getListByRegion(String region, @NotEmpty Pageable pageable) {
        return lectureJpaRepo.findByRegion(region, pageable);
    }

    public Lecture saveLectureAndImage(String email, List<MultipartFile> fileList, Lecture lecture) throws IOException {
        Lecture savedLecture = saveLecture(lecture);

        for (MultipartFile file : fileList) {
            String fileURI = s3Uploader.upload(file, "lecture", email);
            LectureImage lectureImage = LectureImage.builder()
                    .fileURI(fileURI)
                    .lecture(savedLecture)
                    .build();
            lectureImageService.saveLectureImage(lectureImage);
        }
        return savedLecture;
    }
}
