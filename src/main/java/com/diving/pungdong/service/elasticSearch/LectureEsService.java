package com.diving.pungdong.service.elasticSearch;

import com.diving.pungdong.advice.exception.ResourceNotFoundException;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.elasticSearch.LectureEs;
import com.diving.pungdong.dto.equipment.create.EquipmentCreateInfo;
import com.diving.pungdong.dto.equipment.create.EquipmentInfo;
import com.diving.pungdong.dto.lecture.list.LectureInfo;
import com.diving.pungdong.dto.lecture.update.LectureUpdateInfo;
import com.diving.pungdong.repo.elasticSearch.LectureEsRepo;
import com.diving.pungdong.repo.lecture.LectureJpaRepo;
import com.diving.pungdong.service.LectureMarkService;
import com.diving.pungdong.service.LectureService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LectureEsService {
    private final LectureEsRepo lectureEsRepo;
    private final LectureJpaRepo lectureJpaRepo;
    private final LectureService lectureService;
    private final LectureMarkService lectureMarkService;
    private final ModelMapper modelMapper;

    @Transactional
    public void saveLectureInfo(Account creator, Long lectureId) {
        lectureService.checkLectureCreator(creator, lectureId);

        Lecture lecture = lectureService.findLectureById(lectureId);
        String lectureImageUrl = lectureService.findMainLectureImage(lecture);
        List<String> equipmentNames = lectureService.mapToEquipmentNames(lecture);

        LectureEs lectureEs = LectureEs.builder()
                .id(lecture.getId())
                .nickName(lecture.getInstructor().getNickName())
                .title(lecture.getTitle())
                .region(lecture.getRegion())
                .level(lecture.getLevel())
                .maxNumber(lecture.getPeriod())
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

    public Page<LectureInfo> getListContainKeyword(Account account, String keyword, Pageable pageable) {
        Page<LectureEs> lectureEsPage = lectureEsRepo.findByTitleOrNickName(keyword, keyword, pageable);
        Map<Long, Boolean> likeLectureMap = lectureMarkService.findLikeLectureMap(account);

        List<LectureInfo> lectureInfos = new ArrayList<>();
        for (LectureEs lectureEs : lectureEsPage) {
            LectureInfo lectureInfo = modelMapper.map(lectureEs, LectureInfo.class);
            boolean isMarked = likeLectureMap.getOrDefault(lectureEs.getId(), false);
            lectureInfo.setIsMarked(isMarked);

            lectureInfos.add(lectureInfo);
        }

        return new PageImpl<>(lectureInfos, lectureEsPage.getPageable(), lectureEsPage.getContent().size());
    }

    public void updateLectureInfo(LectureUpdateInfo lectureUpdateInfo) {
        LectureEs lectureEs = lectureEsRepo.findById(String.valueOf(lectureUpdateInfo.getId())).orElseThrow(ResourceNotFoundException::new);

        lectureEs.updateLectureInfo(lectureUpdateInfo);
        lectureEsRepo.save(lectureEs);
    }

    public void updateInstructorNickName(Account account, String newNickName) {
        List<LectureEs> lectureEsList = lectureEsRepo.findAllByNickName(account.getNickName());

        for (LectureEs lectureEs : lectureEsList) {
            lectureEs.setNickName(newNickName);
        }

        lectureEsRepo.saveAll(lectureEsList);
    }

    public void updateEquipmentNames(EquipmentCreateInfo info) {
        Optional<LectureEs> lectureEsOptional = lectureEsRepo.findById(String.valueOf(info.getLectureId()));

        if (lectureEsOptional.isPresent()) {
            LectureEs lectureEs = lectureEsOptional.get();
            List<String> equipmentNames = new ArrayList<>();
            for (EquipmentInfo equipmentInfo : info.getEquipmentInfos()) {
                equipmentNames.add(equipmentInfo.getName());
            }

            lectureEs.getEquipmentNames().addAll(equipmentNames);
            lectureEsRepo.save(lectureEs);
        }
    }

    public void updateMainLectureImage(Long lectureId) {
        Lecture lecture = lectureJpaRepo.findByIdWithImages(lectureId).orElseThrow(ResourceNotFoundException::new);

        if (!lecture.getLectureImages().isEmpty()) {
            LectureEs lectureEs = lectureEsRepo.findById(String.valueOf(lectureId)).orElseThrow(ResourceNotFoundException::new);

            lectureEs.setImageUrl(lecture.getLectureImages().get(0).getFileURI());
            lectureEsRepo.save(lectureEs);
        }
    }
}