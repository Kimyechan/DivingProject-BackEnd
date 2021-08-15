package com.diving.pungdong.repo;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.review.Review;
import com.netflix.discovery.converters.Auto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ReviewJpaRepoTest {
    @Autowired
    private ReviewJpaRepo reviewJpaRepo;

    @Autowired
    TestEntityManager em;

    public Lecture createReviews() {
        Lecture lecture = Lecture.builder().build();
        em.persist(lecture);

        for (int i = 1; i <= 5; i++) {
            Review review = Review.builder()
                    .instructorStar((float) i)
                    .lectureStar((float) i)
                    .locationStar((float) i)
                    .writeDate(LocalDate.now().minusDays(i))
                    .lecture(lecture)
                    .build();
            em.persist(review);
        }

        em.flush();
        em.clear();

        return lecture;
    }

    @Test
    @DisplayName("해당 강의 리뷰 목록 평점 높은 순서대로 조회")
    public void findOrderByStarAvgDesc() {
        Lecture lecture = createReviews();

        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "totalStarAvg"));
        Page<Review> reviews = reviewJpaRepo.findByLecture(lecture, pageable);

        assertThat(reviews.getContent().get(0).getTotalStarAvg()).isEqualTo(5.0f);
    }

    @Test
    @DisplayName("해당 강의 리뷰 목록 평점 낮은 순서대로 조회")
    public void findOrderByStarAvgAsc() {
        Lecture lecture = createReviews();

        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "totalStarAvg"));
        Page<Review> reviews = reviewJpaRepo.findByLecture(lecture, pageable);

        assertThat(reviews.getContent().get(0).getTotalStarAvg()).isEqualTo(1.0f);
    }

    @Test
    @DisplayName("해당 강의 리뷰 목록 최신 순서대로 조회")
    public void findOrderBy() {
        Lecture lecture = createReviews();

        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "writeDate"));
        Page<Review> reviews = reviewJpaRepo.findByLecture(lecture, pageable);

        assertThat(reviews.getContent().get(0).getWriteDate()).isEqualTo(LocalDate.now().minusDays(1));
    }

    public Account createMyReviews() {
        Account account = Account.builder().build();
        Account savedAccount = em.persist(account);

        Lecture lecture = Lecture.builder().build();
        em.persist(lecture);

        for (int i = 1; i <= 5; i++) {
            Review review = Review.builder()
                    .instructorStar((float) i)
                    .lectureStar((float) i)
                    .locationStar((float) i)
                    .writeDate(LocalDate.now().minusDays(i))
                    .lecture(lecture)
                    .writer(account)
                    .build();
            em.persist(review);
        }

        em.flush();
        em.clear();

        return savedAccount;
    }

    @Test
    @DisplayName("나의 강의 리뷰 목록 읽기")
    public void findByWriter() {
        //given
        Account writer = createMyReviews();
        Pageable pageable = PageRequest.of(0, 5);

        //when
        Page<Review> reviewPage = reviewJpaRepo.findByWriter(writer, pageable);

        //then
        for (Review review : reviewPage.getContent()) {
            assertThat(review.getWriter()).isEqualTo(writer);
        }
    }
}