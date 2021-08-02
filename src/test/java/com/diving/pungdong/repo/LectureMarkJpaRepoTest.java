package com.diving.pungdong.repo;

import com.diving.pungdong.domain.LectureMark;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.lecture.Lecture;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class LectureMarkJpaRepoTest {
    @Autowired
    private LectureMarkJpaRepo lectureMarkJpaRepo;

    @Autowired
    private TestEntityManager em;

    public Account saveMarkedLectures() {
        Account account = Account.builder().build();
        em.persist(account);

        for (int i = 0; i < 5; i++) {
            Lecture lecture = Lecture.builder().build();
            em.persist(lecture);

            LectureMark lectureMark = LectureMark.builder()
                    .account(account)
                    .lecture(lecture)
                    .build();
            em.persist(lectureMark);
        }

        em.flush();
        em.clear();

        return account;

    }

    @Test
    @DisplayName("좋아요된 강의를 같이 불러오는지 확인합니다")
    public void checkLoadLikeLectures() {
        Account account = saveMarkedLectures();
        Pageable pageable = PageRequest.of(0, 5);

        Page<LectureMark> lectureMarkPage = lectureMarkJpaRepo.findByAccount(account, pageable);

        for (LectureMark lectureMark : lectureMarkPage.getContent()) {
            assertTrue(Hibernate.isInitialized(lectureMark.getLecture()));
        }
    }

    @Test
    @DisplayName("한 강의의 좋아요 여부 조회 - 좋아요 안 했을 때")
    public void findByAccountAndLectureEmpty() {
        // given
        LectureMark savedLectureMark = saveSingleLectureMark();
        Long accountId = savedLectureMark.getAccount().getId();
        Long lectureId = savedLectureMark.getLecture().getId();

        // when
        Optional<LectureMark> lectureMark = lectureMarkJpaRepo.findByAccountAndLecture(accountId, lectureId);

        // then
        assertTrue(lectureMark.isPresent());
    }

    @Test
    @DisplayName("한 강의의 좋아요 여부 조회 - 좋아요 했을 때")
    public void findByAccountAndLectureNotEmpty() {
        // given
        LectureMark savedLectureMark = saveSingleLectureMark();
        Long accountId = savedLectureMark.getAccount().getId();
        Long lectureId = savedLectureMark.getLecture().getId();

        // when
        Optional<LectureMark> lectureMark = lectureMarkJpaRepo.findByAccountAndLecture(accountId, 2L);

        // then
        assertFalse(lectureMark.isPresent());
    }

    private LectureMark saveSingleLectureMark() {
        Account account = Account.builder().build();
        Account savedAccount = em.persist(account);

        Lecture lecture = Lecture.builder().build();
        Lecture savedLecture = em.persist(lecture);

        LectureMark lectureMark = LectureMark.builder()
                .account(savedAccount)
                .lecture(savedLecture)
                .build();

        LectureMark savedLectureMark = lectureMarkJpaRepo.save(lectureMark);

        em.flush();
        em.clear();

        return savedLectureMark;
    }
}