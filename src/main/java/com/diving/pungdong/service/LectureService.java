package com.diving.pungdong.service;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.advice.exception.NoPermissionsException;
import com.diving.pungdong.domain.LectureMark;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.equipment.Equipment;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.schedule.Schedule;
import com.diving.pungdong.domain.schedule.ScheduleDateTime;
import com.diving.pungdong.dto.lecture.LectureCreatorInfo;
import com.diving.pungdong.dto.lecture.create.LectureCreateInfo;
import com.diving.pungdong.dto.lecture.create.LectureCreateResult;
import com.diving.pungdong.dto.lecture.detail.LectureDetail;
import com.diving.pungdong.dto.lecture.like.list.LikeLectureInfo;
import com.diving.pungdong.dto.lecture.like.mark.MarkLectureResult;
import com.diving.pungdong.dto.lecture.list.LectureInfo;
import com.diving.pungdong.dto.lecture.list.mylist.MyLectureInfo;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LectureService {
    private final LectureJpaRepo lectureJpaRepo;
    private final LectureMarkService lectureMarkService;

    public Lecture saveLecture(Lecture lecture) {
        return lectureJpaRepo.save(lecture);
    }

    public Lecture findLectureById(Long id) {
        return lectureJpaRepo.findById(id).orElseThrow(BadRequestException::new);
    }

    @Transactional
    public Lecture updateLecture(LectureUpdateInfo lectureUpdateInfo, Account account) {
        Lecture lecture = findLectureById(lectureUpdateInfo.getId());
        checkLectureCreator(account, lecture.getId());

        lecture.setTitle(lectureUpdateInfo.getTitle());
        lecture.setClassKind(lectureUpdateInfo.getClassKind());
        lecture.setOrganization(lectureUpdateInfo.getOrganization());
        lecture.setLevel(lectureUpdateInfo.getLevel());
        lecture.setDescription(lectureUpdateInfo.getDescription());
        lecture.setPrice(lectureUpdateInfo.getPrice());
        lecture.setRegion(lectureUpdateInfo.getRegion());
        lecture.setMaxNumber(lectureUpdateInfo.getMaxNumber());
        lecture.setPeriod(lectureUpdateInfo.getPeriod());
        lecture.setLectureTime(lectureUpdateInfo.getLectureTime());
        lecture.setServiceTags(lectureUpdateInfo.getServiceTags());

        return lectureJpaRepo.save(lecture);
    }

    public void deleteLectureById(Long id) {
        lectureJpaRepo.deleteById(id);
    }

    public Page<MyLectureInfo> findMyLectureInfoList(Account instructor, Pageable pageable) {
        Page<Lecture> lecturePage = lectureJpaRepo.findByInstructor(instructor, pageable);
        List<Lecture> lectureList = lecturePage.getContent();

        List<MyLectureInfo> myLectureInfoList = mapToMyLectureInfoList(lectureList);

        return new PageImpl<>(myLectureInfoList, pageable, lecturePage.getTotalElements());
    }

    public List<MyLectureInfo> mapToMyLectureInfoList(List<Lecture> lectureList) {
        List<MyLectureInfo> myLectureInfoList = new ArrayList<>();

        for (Lecture lecture : lectureList) {
            String lectureImageUrl = getMainLectureImage(lecture);
            List<String> equipmentNames = mapToEquipmentNames(lecture);
            Long leftScheduleDate = calcLeftScheduleDate(lecture.getSchedules());

            MyLectureInfo myLectureInfo = MyLectureInfo.builder()
                    .id(lecture.getId())
                    .title(lecture.getTitle())
                    .organization(lecture.getOrganization())
                    .level(lecture.getLevel())
                    .region(lecture.getRegion())
                    .maxNumber(lecture.getMaxNumber())
                    .period(lecture.getPeriod())
                    .lectureTime(lecture.getLectureTime())
                    .imageUrl(lectureImageUrl)
                    .price(lecture.getPrice())
                    .equipmentNames(equipmentNames)
                    .leftScheduleDate(leftScheduleDate)
                    .isClosed(lecture.getIsClosed())
                    .build();

            myLectureInfoList.add(myLectureInfo);
        }

        myLectureInfoList.sort(Comparator.comparing(MyLectureInfo::getLeftScheduleDate));

        return myLectureInfoList;
    }

    public Long calcLeftScheduleDate(List<Schedule> schedules) {
        Long latestLeftScheduleDate = 365L;

        for (Schedule schedule : schedules) {
            for (ScheduleDateTime scheduleDateTime : schedule.getScheduleDateTimes()) {
                if (scheduleDateTime.getDate().isBefore(LocalDate.now())) {
                    continue;
                } else if (latestLeftScheduleDate > ChronoUnit.DAYS.between(LocalDate.now(), scheduleDateTime.getDate())) {
                    latestLeftScheduleDate = ChronoUnit.DAYS.between(LocalDate.now(), scheduleDateTime.getDate());
                }
            }
        }

        return latestLeftScheduleDate;
    }

    public void checkRightInstructor(Account account, Long lectureId) {
        Lecture lecture = findLectureById(lectureId);
        if (!account.getId().equals(lecture.getInstructor().getId())) {
            throw new NoPermissionsException();
        }
    }

    @Transactional(readOnly = true)
    public Page<NewLectureInfo> getNewLecturesInfo(Account account, Pageable pageable) {
        LocalDateTime pastDateTime = LocalDateTime.now().minusDays(15);
        Page<Lecture> lecturePage = lectureJpaRepo.findLectureByRegistrationDateAfter(pastDateTime, pageable);

        List<NewLectureInfo> newLectureInfos = mapToNewLectureInfos(account, lecturePage);

        return new PageImpl<>(newLectureInfos, lecturePage.getPageable(), lecturePage.getContent().size());
    }

    @Transactional(readOnly = true)
    public List<NewLectureInfo> mapToNewLectureInfos(Account account, Page<Lecture> lecturePage) {
        List<NewLectureInfo> newLectureInfos = new ArrayList<>();
        Map<Long, Boolean> likeLectureMap = lectureMarkService.findLikeLectureMap(account);

        for (Lecture lecture : lecturePage.getContent()) {
            String lectureImageUrl = getMainLectureImage(lecture);
            boolean isMarked = likeLectureMap.getOrDefault(lecture.getId(), false);
            List<String> equipmentNames = mapToEquipmentNames(lecture);

            NewLectureInfo newLectureInfo = NewLectureInfo.builder()
                    .id(lecture.getId())
                    .title(lecture.getTitle())
                    .organization(lecture.getOrganization())
                    .level(lecture.getLevel())
                    .region(lecture.getRegion())
                    .period(lecture.getPeriod())
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

    @Transactional(readOnly = true)
    public boolean isLectureMarked(Account account, Long lectureId) {
        if (account == null) {
            return false;
        }

        List<LectureMark> lectureMarks = lectureMarkService.findAllLectureMarkByAccount(account);

        boolean isMarked = false;
        for (LectureMark lectureMark : lectureMarks) {
            if (lectureMark.getLecture().getId().equals(lectureId)) {
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
        Map<Long, Boolean> likeLectureMap = lectureMarkService.findLikeLectureMap(account);

        for (Lecture lecture : lecturePage.getContent()) {
            String lectureImageUrl = getMainLectureImage(lecture);
            boolean isMarked = likeLectureMap.getOrDefault(lecture.getId(), false);
            List<String> equipmentNames = mapToEquipmentNames(lecture);

            LectureInfo lectureInfo = LectureInfo.builder()
                    .id(lecture.getId())
                    .title(lecture.getTitle())
                    .organization(lecture.getOrganization())
                    .level(lecture.getLevel())
                    .region(lecture.getRegion())
                    .maxNumber(lecture.getMaxNumber())
                    .period(lecture.getPeriod())
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

    public List<String> mapToEquipmentNames(Lecture lecture) {
        List<String> equipmentNames = new ArrayList<>();
        for (Equipment equipment : lecture.getEquipmentList()) {
            equipmentNames.add(equipment.getName());
        }

        return equipmentNames;
    }

    public String getMainLectureImage(Lecture lecture) {
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
                .period(lectureCreateInfo.getPeriod())
                .lectureTime(lectureCreateInfo.getLectureTime())
                .serviceTags(lectureCreateInfo.getServiceTags())
                .build();

        Lecture savedLecture = lectureJpaRepo.save(lecture);

        return LectureCreateResult.builder()
                .lectureId(savedLecture.getId())
                .build();
    }

    @Transactional(readOnly = true)
    public void checkLectureCreator(Account account, Long lectureId) {
        Lecture lecture = findLectureById(lectureId);

        if (!lecture.getInstructor().getId().equals(account.getId())) {
            throw new BadRequestException();
        }
    }

    @Transactional(readOnly = true)
    public Page<LectureInfo> filterSearchList(Account account, FilterSearchCondition condition, Pageable pageable) {
        Page<Lecture> lecturePage = lectureJpaRepo.searchListByCondition(condition, pageable);
        List<LectureInfo> lectureInfos = mapToPopularLectureInfos(account, lecturePage);

        return new PageImpl<>(lectureInfos, lecturePage.getPageable(), lecturePage.getContent().size());
    }

    public LectureCreatorInfo findLectureCreatorInfo(Long lectureId) {
        Lecture lecture = findLectureById(lectureId);
        Account account = lecture.getInstructor();

        return LectureCreatorInfo.builder()
                .instructorId(account.getId())
                .nickName(account.getNickName())
                .selfIntroduction(account.getSelfIntroduction())
                .profilePhotoUrl(account.getProfilePhoto().getImageUrl())
                .build();
    }

    @Transactional(readOnly = true)
    public LectureDetail findLectureDetailInfo(Long id) {
        Lecture lecture = findLectureById(id);

        return LectureDetail.builder()
                .id(lecture.getId())
                .title(lecture.getTitle())
                .classKind(lecture.getClassKind())
                .organization(lecture.getOrganization())
                .level(lecture.getLevel())
                .maxNumber(lecture.getMaxNumber())
                .period(lecture.getPeriod())
                .description(lecture.getDescription())
                .price(lecture.getPrice())
                .region(lecture.getRegion())
                .reviewTotalAvg(lecture.getReviewTotalAvg())
                .reviewCount(lecture.getReviewCount())
                .serviceTags(lecture.getServiceTags())
                .isClosed(lecture.getIsClosed())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<Lecture> findLikeLectures(Account account, Pageable pageable) {
        Page<LectureMark> lectureMarks = lectureMarkService.findLectureMarksByAccount(account, pageable);

        List<Lecture> likeLectureList = new ArrayList<>();
        for (LectureMark lectureMark : lectureMarks) {
            likeLectureList.add(lectureMark.getLecture());
        }

        return new PageImpl<>(likeLectureList, lectureMarks.getPageable(), lectureMarks.getTotalElements());
    }

    public Page<LikeLectureInfo> mapToLikeLectureInfos(Page<Lecture> likeLecturePage) {
        List<LikeLectureInfo> likeLectureInfos = new ArrayList<>();
        for (Lecture lecture : likeLecturePage.getContent()) {
            String lectureImageUrl = getMainLectureImage(lecture);
            List<String> equipmentNames = mapToEquipmentNames(lecture);

            LikeLectureInfo lectureInfo = LikeLectureInfo.builder()
                    .id(lecture.getId())
                    .title(lecture.getTitle())
                    .organization(lecture.getOrganization())
                    .level(lecture.getLevel())
                    .region(lecture.getRegion())
                    .maxNumber(lecture.getMaxNumber())
                    .period(lecture.getPeriod())
                    .lectureTime(lecture.getLectureTime())
                    .imageUrl(lectureImageUrl)
                    .reviewCount(lecture.getReviewCount())
                    .starAvg(lecture.getReviewTotalAvg())
                    .price(lecture.getPrice())
                    .equipmentNames(equipmentNames)
                    .isMarked(true)
                    .build();

            likeLectureInfos.add(lectureInfo);
        }

        return new PageImpl<>(likeLectureInfos, likeLecturePage.getPageable(), likeLecturePage.getTotalElements());
    }

    @Transactional
    public MarkLectureResult markLecture(Account account, Long lectureId) {
        lectureMarkService.checkMarkLecture(account.getId(), lectureId);
        Lecture lecture = findLectureById(lectureId);

        LectureMark lectureMark = lectureMarkService.saveLectureMark(account, lecture);

        return MarkLectureResult.builder()
                .lectureMarkId(lectureMark.getId())
                .accountId(account.getId())
                .lectureId(lecture.getId())
                .build();
    }

    @Transactional
    public void unmarkLecture(Long accountId, Long lectureId) {
        lectureMarkService.deleteMarkLecture(accountId, lectureId);
    }

    @Transactional
    public void closeAllLecture(Account account) {
        List<Lecture> lectures = lectureJpaRepo.findByInstructor(account);

        for (Lecture lecture : lectures) {
            lecture.setIsClosed(true);
        }
    }

    @Transactional
    public void updateLectureClosed(Account account, Long lectureId, Boolean isClosed) {
        checkLectureCreator(account, lectureId);

        Lecture lecture = findLectureById(lectureId);
        lecture.setIsClosed(isClosed);
    }
}
