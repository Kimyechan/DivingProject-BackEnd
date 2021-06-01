package com.diving.pungdong.service.elasticSearch;

import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.elasticSearch.LectureEs;
import com.diving.pungdong.repo.elasticSearch.LectureEsRepo;
import com.diving.pungdong.service.LectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LectureEsService {
    private final LectureEsRepo lectureEsRepo;
    private final LectureService lectureService;

    @Transactional
    public void saveLectureInfo(Long lectureId) {
        Lecture lecture = lectureService.getLectureById(lectureId);
        String lectureImageUrl = lectureService.getMainLectureImage(lecture);
        List<String> equipmentNames = lectureService.mapToEquipmentNames(lecture);

        LectureEs lectureEs = LectureEs.builder()
                .id(lecture.getId())
                .nickName(lecture.getInstructor().getNickName())
                .title(lecture.getTitle())
                .region(lecture.getRegion())
                .level(lecture.getLevel())
                .maxNumber(lecture.getMaxNumber())
                .lectureTime(lecture.getLectureTime())
                .price(lecture.getPrice())
                .organization(lecture.getOrganization())
                .reviewCount(lecture.getReviewCount())
                .starAvg(lecture.getReviewTotalAvg())
                .imageUrl(lectureImageUrl)
                .equipmentNames(equipmentNames)
                .build();

        lectureEsRepo.save(lectureEs);
    }
}
