package com.diving.pungdong.service;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.advice.exception.ResourceNotFoundException;
import com.diving.pungdong.domain.LectureMark;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.repo.LectureMarkJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class LectureMarkService {
    private final LectureMarkJpaRepo lectureMarkJpaRepo;

    @Transactional(readOnly = true)
    public Page<LectureMark> findLectureMarksByAccount(Account account, Pageable pageable) {
        return lectureMarkJpaRepo.findByAccount(account, pageable);
    }

    public LectureMark saveLectureMark(Account account, Lecture lecture) {
        LectureMark lectureMark = LectureMark.builder()
                .lecture(lecture)
                .account(account)
                .build();

        return lectureMarkJpaRepo.save(lectureMark);
    }

    public void checkMarkLecture(Long accountId, Long lectureId) {
        Optional<LectureMark> lectureMarkOptional = lectureMarkJpaRepo.findByAccountAndLecture(accountId, lectureId);

        if (lectureMarkOptional.isPresent()) {
            throw new BadRequestException("이미 찜한 강의입니다");
        }
    }

    public void deleteMarkLecture(Long accountId, Long lectureId) {
        LectureMark lectureMark = lectureMarkJpaRepo.findByAccountAndLecture(accountId, lectureId).orElseThrow(ResourceNotFoundException::new);

        lectureMarkJpaRepo.deleteById(lectureMark.getId());
    }

    @Transactional(readOnly = true)
    public Map<Long, Boolean> findLikeLectureMap(Account account) {
        Map<Long, Boolean> likeLectureMap = new HashMap<>();
        if (account == null) {
            return likeLectureMap;
        }

        List<LectureMark> lectureMarks = lectureMarkJpaRepo.findByAccount(account);
        for (LectureMark lectureMark : lectureMarks) {
            Lecture likeLecture = lectureMark.getLecture();
            likeLectureMap.put(likeLecture.getId(), true);
        }

        return likeLectureMap;
    }

    @Transactional(readOnly = true)
    public List<LectureMark> findAllLectureMarkByAccount(Account account) {
        return lectureMarkJpaRepo.findByAccount(account);
    }

    @Transactional(readOnly = true)
    public boolean existLectureMark(Account account, Long lectureId) {
        if (account == null) {
            return false;
        }

        Optional<LectureMark> lectureMark = lectureMarkJpaRepo.findByAccountAndLecture(account.getId(), lectureId);

        return lectureMark.isPresent();
    }
}
