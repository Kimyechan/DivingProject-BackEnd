package com.diving.pungdong.service;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.advice.exception.NoPermissionsException;
import com.diving.pungdong.domain.LectureMark;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.equipment.Equipment;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.domain.schedule.ScheduleDetail;
import com.diving.pungdong.dto.lecture.create.LectureCreateInfo;
import com.diving.pungdong.dto.lecture.create.LectureCreateResult;
import com.diving.pungdong.dto.lecture.list.LectureInfo;
import com.diving.pungdong.dto.lecture.list.newList.NewLectureInfo;
import com.diving.pungdong.dto.lecture.list.search.FilterSearchCondition;
import com.diving.pungdong.dto.lecture.update.LectureUpdateInfo;
import com.diving.pungdong.repo.lecture.LectureJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LectureService {
    private final LectureJpaRepo lectureJpaRepo;

    public Lecture saveLecture(Lecture lecture) {
        return lectureJpaRepo.save(lecture);
    }

    public Lecture getLectureById(Long id) {
        return lectureJpaRepo.findById(id).orElseThrow(BadRequestException::new);
    }

    public Lecture updateLecture(LectureUpdateInfo lectureUpdateInfo, Lecture lecture) {
        lecture.setTitle(lectureUpdateInfo.getTitle());
        lecture.setClassKind(lectureUpdateInfo.getClassKind());
        lecture.setOrganization(lectureUpdateInfo.getOrganization());
        lecture.setLevel(lectureUpdateInfo.getLevel());
        lecture.setDescription(lectureUpdateInfo.getDescription());
        lecture.setPrice(lectureUpdateInfo.getPrice());
        lecture.setRegion(lectureUpdateInfo.getRegion());

        return lectureJpaRepo.save(lecture);
    }

//    public Lecture updateLectureTx(String email, LectureUpdateInfo lectureUpdateInfo, List<MultipartFile> addLectureImageFiles, Lecture lecture) throws IOException {
//        lectureImageService.deleteIfIsDeleted(lectureUpdateInfo);
//        lectureImageService.addList(email, addLectureImageFiles, lecture);
//        equipmentService.lectureEquipmentUpdate(lectureUpdateInfo.getEquipmentUpdateList(), lecture);
//
//        return updateLecture(lectureUpdateInfo, lecture);
//    }

    public void deleteLectureById(Long id) {
        lectureJpaRepo.deleteById(id);
    }

    public Page<com.diving.pungdong.dto.lecture.list.mylist.LectureInfo> getMyLectureInfoList(Account instructor, Pageable pageable) {
        Page<Lecture> lecturePage = lectureJpaRepo.findByInstructor(instructor, pageable);
        List<Lecture> lectureList = lecturePage.getContent();

        List<com.diving.pungdong.dto.lecture.list.mylist.LectureInfo> lectureInfoList = mapToLectureInfoList(lectureList);

        return new PageImpl<>(lectureInfoList, pageable, lecturePage.getTotalElements());
    }

    public List<com.diving.pungdong.dto.lecture.list.mylist.LectureInfo> mapToLectureInfoList(List<Lecture> lectureList) {
        List<com.diving.pungdong.dto.lecture.list.mylist.LectureInfo> lectureInfoList = new ArrayList<>();

        for (Lecture lecture : lectureList) {
            Integer upcomingScheduleCount = countUpcomingSchedule(lecture);

            com.diving.pungdong.dto.lecture.list.mylist.LectureInfo lectureInfo = com.diving.pungdong.dto.lecture.list.mylist.LectureInfo.builder()
                    .lectureId(lecture.getId())
                    .title(lecture.getTitle())
                    .organization(lecture.getOrganization())
                    .level(lecture.getLevel())
                    .cost(lecture.getPrice())
                    .isRentEquipment(!lecture.getEquipmentList().isEmpty())
                    .upcomingScheduleCount(upcomingScheduleCount)
                    .build();

            lectureInfoList.add(lectureInfo);
        }

        return lectureInfoList;
    }

    public Integer countUpcomingSchedule(Lecture lecture) {
        Integer upcomingScheduleCount = 0;

        for (Schedule schedule : lecture.getSchedules()) {
            exitFor:
            for (ScheduleDetail scheduleDetail : schedule.getScheduleDetails()) {
                LocalDate upcomingScheduleDate = LocalDate.now().plusDays(14);
                if (scheduleDetail.getDate().isAfter(LocalDate.now().minusDays(1))
                        && scheduleDetail.getDate().isBefore(upcomingScheduleDate.plusDays(1))) {
                    upcomingScheduleCount += 1;
                    break exitFor;
                }
            }
        }

        return upcomingScheduleCount;
    }

    public void checkRightInstructor(Account account, Long lectureId) {
        Lecture lecture = getLectureById(lectureId);
        if (!account.getId().equals(lecture.getInstructor().getId())) {
            throw new NoPermissionsException();
        }
    }

    public Page<NewLectureInfo> getNewLecturesInfo(Account account, Pageable pageable) {
        LocalDateTime pastDateTime = LocalDateTime.now().minusDays(15);
        Page<Lecture> lecturePage = lectureJpaRepo.findLectureByRegistrationDateAfter(pastDateTime, pageable);

        List<NewLectureInfo> newLectureInfos = mapToNewLectureInfos(account, lecturePage);

        return new PageImpl<>(newLectureInfos, lecturePage.getPageable(), lecturePage.getContent().size());
    }

    public List<NewLectureInfo> mapToNewLectureInfos(Account account, Page<Lecture> lecturePage) {
        List<NewLectureInfo> newLectureInfos = new ArrayList<>();
        for (Lecture lecture : lecturePage.getContent()) {
            String lectureImageUrl = getMainLectureImage(lecture);
            boolean isMarked = isLectureMarked(account, lecture.getLectureMarks());
            List<String> equipmentNames = new ArrayList<>();
            for (Equipment equipment : lecture.getEquipmentList()) {
                equipmentNames.add(equipment.getName());
            }

            NewLectureInfo newLectureInfo = NewLectureInfo.builder()
                    .id(lecture.getId())
                    .title(lecture.getTitle())
                    .organization(lecture.getOrganization())
                    .level(lecture.getLevel())
                    .region(lecture.getRegion())
                    .maxNumber(lecture.getMaxNumber())
                    .lectureTime(lecture.getLectureTime())
                    .imageUrl(lectureImageUrl)
                    .isMarked(isMarked)
                    .price(lecture.getPrice())
                    .equipmentNames(equipmentNames)
                    .build();

            newLectureInfos.add(newLectureInfo);
        }

        return newLectureInfos;
    }

    public boolean isLectureMarked(Account account, List<LectureMark> lectureMarks) {
        if (account == null) {
            return false;
        }

        boolean isMarked = false;
        for (LectureMark lectureMark : lectureMarks) {
            if (lectureMark.getAccount().getId().equals(account.getId())) {
                isMarked = true;
                break;
            }
        }
        return isMarked;
    }

    @Transactional(readOnly = true)
    public Page<LectureInfo> getPopularLecturesInfo(Account account, Pageable pageable) {
        Page<Lecture> lecturePage = lectureJpaRepo.findPopularLectures(pageable);
        List<LectureInfo> lectureInfos = mapToPopularLectureInfos(account, lecturePage);

        return new PageImpl<>(lectureInfos, lecturePage.getPageable(), lecturePage.getContent().size());
    }

    @Transactional(readOnly = true)
    public List<LectureInfo> mapToPopularLectureInfos(Account account, Page<Lecture> lecturePage) {
        List<LectureInfo> lectureInfos = new ArrayList<>();
        for (Lecture lecture : lecturePage.getContent()) {
            String lectureImageUrl = getMainLectureImage(lecture);
            boolean isMarked = isLectureMarked(account, lecture.getLectureMarks());
            List<String> equipmentNames = new ArrayList<>();
            for (Equipment equipment : lecture.getEquipmentList()) {
                equipmentNames.add(equipment.getName());
            }

            LectureInfo lectureInfo = LectureInfo.builder()
                    .id(lecture.getId())
                    .title(lecture.getTitle())
                    .organization(lecture.getOrganization())
                    .level(lecture.getLevel())
                    .region(lecture.getRegion())
                    .maxNumber(lecture.getMaxNumber())
                    .lectureTime(lecture.getLectureTime())
                    .imageUrl(lectureImageUrl)
                    .reviewCount(lecture.getReviewCount())
                    .starAvg(lecture.getReviewTotalAvg())
                    .isMarked(isMarked)
                    .price(lecture.getPrice())
                    .equipmentNames(equipmentNames)
                    .build();

            lectureInfos.add(lectureInfo);
        }

        return lectureInfos;
    }

    private String getMainLectureImage(Lecture lecture) {
        if (!lecture.getLectureImages().isEmpty()) {
            return lecture.getLectureImages().get(0).getFileURI();
        } else {
            return "";
        }
    }

    @Transactional
    public LectureCreateResult createLecture(Account account, LectureCreateInfo lectureCreateInfo) {
        Lecture lecture = Lecture.builder()
                .instructor(account)
                .title(lectureCreateInfo.getTitle())
                .region(lectureCreateInfo.getRegion())
                .classKind(lectureCreateInfo.getClassKind())
                .organization(lectureCreateInfo.getOrganization())
                .level(lectureCreateInfo.getLevel())
                .description(lectureCreateInfo.getDescription())
                .price(lectureCreateInfo.getPrice())
                .maxNumber(lectureCreateInfo.getMaxNumber())
                .lectureTime(lectureCreateInfo.getLectureTime())
                .build();

        Lecture savedLecture = lectureJpaRepo.save(lecture);

        return LectureCreateResult.builder()
                .lectureId(savedLecture.getId())
                .build();
    }

    @Transactional(readOnly = true)
    public void checkLectureCreator(Account account, Long lectureId) {
        Lecture lecture = getLectureById(lectureId);

        if (!lecture.getInstructor().getId().equals(account.getId())) {
            throw new BadRequestException();
        }
    }

    public Page<LectureInfo> filterSearchList(Account account, FilterSearchCondition condition, Pageable pageable) {
        Page<Lecture> lecturePage = lectureJpaRepo.searchListByCondition(condition, pageable);
        List<LectureInfo> lectureInfos = mapToPopularLectureInfos(account, lecturePage);

        return new PageImpl<>(lectureInfos, lecturePage.getPageable(), lecturePage.getContent().size());
    }
}
