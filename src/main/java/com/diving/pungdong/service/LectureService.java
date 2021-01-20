package com.diving.pungdong.service;

import com.diving.pungdong.config.S3Uploader;
import com.diving.pungdong.domain.Location;
import com.diving.pungdong.domain.equipment.Equipment;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.LectureImage;
import com.diving.pungdong.domain.swimmingPool.SwimmingPool;
import com.diving.pungdong.repo.EquipmentJpaRepo;
import com.diving.pungdong.repo.LectureJpaRepo;
import com.diving.pungdong.repo.SwimmingPoolJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.diving.pungdong.controller.lecture.LectureController.LectureUpdateInfo;
import com.diving.pungdong.controller.lecture.LectureController.LectureImageUpdate;
import com.diving.pungdong.controller.lecture.LectureController.EquipmentUpdate;

import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LectureService {
    private final LectureJpaRepo lectureJpaRepo;
    private final LectureImageService lectureImageService;
    private final S3Uploader s3Uploader;
    private final EquipmentJpaRepo equipmentJpaRepo;
    private final SwimmingPoolJpaRepo swimmingPoolJpaRepo;

    public Lecture saveLecture(Lecture lecture) {
        return lectureJpaRepo.save(lecture);
    }

    public Page<Lecture> getListByRegion(String region, @NotEmpty Pageable pageable) {
        return lectureJpaRepo.findByRegion(region, pageable);
    }

    public Lecture createLecture(String email, List<MultipartFile> fileList, Lecture lecture, List<Equipment> equipmentList) throws IOException {
        Lecture savedLecture = saveLecture(lecture);

        for (Equipment equipment : equipmentList) {
            equipment.setLecture(lecture);
            equipmentJpaRepo.save(equipment);
        }

        for (MultipartFile file : fileList) {
            String fileURI = s3Uploader.upload(file, "lecture", email);
            LectureImage lectureImage = LectureImage.builder()
                    .fileURI(fileURI)
                    .lecture(savedLecture)
                    .build();
            lectureImageService.saveLectureImage(lectureImage);
            savedLecture.getLectureImages().add(lectureImage);
        }
        return savedLecture;
    }

    public Lecture getLectureById(Long id) {
        return lectureJpaRepo.findById(id).orElse(new Lecture());
    }

    public Lecture updateLecture(String email, LectureUpdateInfo lectureUpdateInfo, List<MultipartFile> addLectureImageFiles, Lecture lecture) throws IOException {
        Location location = lectureUpdateInfo.getSwimmingPoolLocation();
        SwimmingPool swimmingPool = swimmingPoolJpaRepo.findByLocation(location).orElse(SwimmingPool.builder().location(location).build());
        swimmingPoolJpaRepo.save(swimmingPool);

        lecture.setSwimmingPool(swimmingPool);
        lecture.setTitle(lectureUpdateInfo.getTitle());
        lecture.setClassKind(lectureUpdateInfo.getClassKind());
        lecture.setGroupName(lectureUpdateInfo.getGroupName());
        lecture.setCertificateKind(lectureUpdateInfo.getCertificateKind());
        lecture.setDescription(lectureUpdateInfo.getDescription());
        lecture.setPrice(lectureUpdateInfo.getPrice());
        lecture.setPeriod(lectureUpdateInfo.getPeriod());
        lecture.setStudentCount(lectureUpdateInfo.getStudentCount());
        lecture.setRegion(lectureUpdateInfo.getRegion());

        if (!lectureUpdateInfo.getLectureImageUpdateList().isEmpty()) {
            for (LectureImageUpdate lectureImageUpdate : lectureUpdateInfo.getLectureImageUpdateList()) {
                if (lectureImageUpdate.getIsDeleted()) {
                    lectureImageService.deleteByURL(lectureImageUpdate.getLectureImageURL());
                    s3Uploader.deleteFileFromS3(lectureImageUpdate.getLectureImageURL());
                }
            }
        }

        if (!addLectureImageFiles.isEmpty()) {
            for (MultipartFile file : addLectureImageFiles) {
                String fileURI = s3Uploader.upload(file, "lecture", email);
                LectureImage lectureImage = LectureImage.builder()
                        .fileURI(fileURI)
                        .lecture(lecture)
                        .build();
                lectureImageService.saveLectureImage(lectureImage);
                lecture.getLectureImages().add(lectureImage);
            }
        }

        if (!lectureUpdateInfo.getEquipmentUpdateList().isEmpty()) {
            for (EquipmentUpdate equipmentUpdate : lectureUpdateInfo.getEquipmentUpdateList()) {
                Equipment persistenceEquipment = equipmentJpaRepo.findByName(equipmentUpdate.getName()).orElse(null);
                if (persistenceEquipment == null) {
                    Equipment equipment = Equipment.builder()
                            .name(equipmentUpdate.getName())
                            .price(equipmentUpdate.getPrice())
                            .lecture(lecture)
                            .build();
                    equipmentJpaRepo.save(equipment);
                }

                if (equipmentUpdate.getIsDeleted()) {
                    equipmentJpaRepo.deleteByName(equipmentUpdate.getName());
                }
            }
        }

        return lectureJpaRepo.save(lecture);
    }
}
