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
    private final EquipmentService equipmentService;
    private final SwimmingPoolService swimmingPoolService;

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
            equipmentService.saveEquipment(equipment);
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

    public Lecture updateLecture(LectureUpdateInfo lectureUpdateInfo, Lecture lecture) {
        SwimmingPool updatedSwimmingPool = swimmingPoolService.changeSwimmingPool(lectureUpdateInfo);

        lecture.setSwimmingPool(updatedSwimmingPool);
        lecture.setTitle(lectureUpdateInfo.getTitle());
        lecture.setClassKind(lectureUpdateInfo.getClassKind());
        lecture.setGroupName(lectureUpdateInfo.getGroupName());
        lecture.setCertificateKind(lectureUpdateInfo.getCertificateKind());
        lecture.setDescription(lectureUpdateInfo.getDescription());
        lecture.setPrice(lectureUpdateInfo.getPrice());
        lecture.setPeriod(lectureUpdateInfo.getPeriod());
        lecture.setStudentCount(lectureUpdateInfo.getStudentCount());
        lecture.setRegion(lectureUpdateInfo.getRegion());

        return lectureJpaRepo.save(lecture);
    }

    public Lecture updateLectureTx(String email, LectureUpdateInfo lectureUpdateInfo, List<MultipartFile> addLectureImageFiles, Lecture lecture) throws IOException {
        lectureImageService.deleteIfIsDeleted(lectureUpdateInfo);
        lectureImageService.addList(email, addLectureImageFiles, lecture);
        equipmentService.lectureEquipmentUpdate(lectureUpdateInfo.getEquipmentUpdateList(), lecture);

        return updateLecture(lectureUpdateInfo, lecture);
    }
}
