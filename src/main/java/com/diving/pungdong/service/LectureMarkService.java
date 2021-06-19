package com.diving.pungdong.service;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.domain.LectureMark;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.repo.LectureMarkJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
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
}
