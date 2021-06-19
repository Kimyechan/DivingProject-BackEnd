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
}