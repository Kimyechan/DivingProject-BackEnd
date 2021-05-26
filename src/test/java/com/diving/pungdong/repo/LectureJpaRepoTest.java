package com.diving.pungdong.repo;

import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.LectureImage;
import com.diving.pungdong.domain.lecture.Organization;
import com.diving.pungdong.domain.location.Location;
import com.diving.pungdong.dto.lecture.list.search.CostCondition;
import com.diving.pungdong.dto.lecture.list.search.FilterSearchCondition;
import com.diving.pungdong.repo.lecture.LectureJpaRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@DataJpaTest
class LectureJpaRepoTest {
    @Autowired
    private TestEntityManager em;

    @Autowired
    private LectureJpaRepo lectureJpaRepo;

    @Autowired
    private LectureImageJpaRepo lectureImageJpaReop;

//    @Test
//    @DisplayName("강의 저장")
//    public void save() {
//        Lecture lecture = Lecture.builder()
//                .title("강의1")
//                .description("내용1")
//                .classKind("스쿠버 다이빙")
//                .organization(Organization.AIDA)
//                .level("Level1")
//                .price(100000)
//                .instructor(new Account())
//                .lectureImages(List.of(new LectureImage()))
//                .build();
//
//        Lecture savedLecture = lectureJpaRepo.save(lecture);
//
//        assertThat(savedLecture.getId()).isNotNull();
//    }
//
    public void createLecture(String classKind, Organization organization, String level, String region) {
        for (int i = 0; i < 15; i++) {
            Lecture lecture = Lecture.builder()
                    .organization(Organization.AIDA)
                    .classKind(classKind)
                    .organization(organization)
                    .level(level)
                    .price(100000 * (i + 1))
                    .region(region)
                    .build();

            em.persist(lecture);
        }

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("강의 리스트 조회 - 단체 종류")
    public void searchListByGroupName() {
        createLecture("", Organization.AIDA,"", "");
        FilterSearchCondition searchCondition = FilterSearchCondition.builder()
                .organization(Organization.AIDA)
                .build();

        Pageable pageable = PageRequest.of(0, 5);
        Page<Lecture> lecturePage = lectureJpaRepo.searchListByCondition(searchCondition, pageable);

        assertThat(lecturePage.getContent()).isNotEmpty();
        for (Lecture lecture : lecturePage.getContent()) {
            assertThat(lecture.getOrganization()).isEqualTo(Organization.AIDA);
        }
    }

    @Test
    @DisplayName("강의 리스트 조회 결과 없음- 단체 종류")
    public void searchListByGroupNameFail() {
        createLecture("", Organization.AIDA,"", "");
        FilterSearchCondition searchCondition = FilterSearchCondition.builder()
                .organization(Organization.PADI)
                .build();

        Pageable pageable = PageRequest.of(0, 5);
        Page<Lecture> lecturePage = lectureJpaRepo.searchListByCondition(searchCondition, pageable);

        assertThat(lecturePage.getContent().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("강의 리스트 조회 - 자격증 레벨")
    public void searchListByCertificateKind() {
        createLecture("", null,"LEVEL1", "");
        FilterSearchCondition searchCondition = FilterSearchCondition.builder()
                .level("LEVEL1")
                .build();
        Pageable pageable = PageRequest.of(0, 5);
        Page<Lecture> lecturePage = lectureJpaRepo.searchListByCondition(searchCondition, pageable);

        assertThat(lecturePage.getContent()).isNotEmpty();
        for (Lecture lecture : lecturePage.getContent()) {
            assertThat(lecture.getLevel()).isEqualTo("LEVEL1");
        }
    }

    @Test
    @DisplayName("강의 리스트 조회시 결과 없음 - 자격증 레벨")
    public void searchListByCertificateKindFail() {
        createLecture("", null,"LEVEL1", "");
        FilterSearchCondition searchCondition = FilterSearchCondition.builder()
                .level("LEVEL2")
                .build();
        Pageable pageable = PageRequest.of(0, 5);
        Page<Lecture> lecturePage = lectureJpaRepo.searchListByCondition(searchCondition, pageable);

        assertThat(lecturePage.getContent().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("강의 리스트 조회 - 지역별")
    public void searchListByRegion() {
        createLecture("", null,"", "서울");
        FilterSearchCondition searchCondition = FilterSearchCondition.builder()
                .region("서울")
                .build();
        Pageable pageable = PageRequest.of(0, 5);
        Page<Lecture> lecturePage = lectureJpaRepo.searchListByCondition(searchCondition, pageable);

        assertThat(lecturePage.getContent()).isNotEmpty();
        for (Lecture lecture : lecturePage.getContent()) {
            assertThat(lecture.getRegion()).isEqualTo("서울");
        }
    }

    @Test
    @DisplayName("강의 리스트 조회시 결과 없음 - 지역별")
    public void searchListByRegionFail() {
        createLecture("", null,"", "서울");
        FilterSearchCondition searchCondition = FilterSearchCondition.builder()
                .region("부산")
                .build();
        Pageable pageable = PageRequest.of(0, 5);
        Page<Lecture> lecturePage = lectureJpaRepo.searchListByCondition(searchCondition, pageable);

        assertThat(lecturePage.getContent().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("강의 리스트 조회 - 강의료")
    public void searchListByCost() {
        createLecture("", null,"", "");
        CostCondition costCondition = CostCondition.builder()
                .max(150000)
                .min(10000)
                .build();

        FilterSearchCondition searchCondition = FilterSearchCondition.builder()
                .costCondition(costCondition)
                .build();

        Pageable pageable = PageRequest.of(0, 5);
        Page<Lecture> lecturePage = lectureJpaRepo.searchListByCondition(searchCondition, pageable);

        assertThat(lecturePage.getContent()).isNotEmpty();
        for (Lecture lecture : lecturePage.getContent()) {
            assertThat(lecture.getPrice()).isBetween(costCondition.getMin(), costCondition.getMax());
        }
    }

    @Test
    @DisplayName("강의 리스트 조회 결과 없음- 강의료")
    public void searchListByCostFail() {
        createLecture("", null,"", "");
        CostCondition costCondition = CostCondition.builder()
                .max(10000)
                .min(5000)
                .build();

        FilterSearchCondition searchCondition = FilterSearchCondition.builder()
                .costCondition(costCondition)
                .build();

        Pageable pageable = PageRequest.of(0, 5);
        Page<Lecture> lecturePage = lectureJpaRepo.searchListByCondition(searchCondition, pageable);

        assertThat(lecturePage.getContent()).isEmpty();
    }

    @Test
    @DisplayName("강의 리스트 조회 - 전체 조건")
    public void searchListByAll() {
        createLecture("프리 다이빙",Organization.AIDA,"LEVEL1", "서울");
        CostCondition costCondition = CostCondition.builder()
                .max(1500000)
                .min(1000)
                .build();

        FilterSearchCondition searchCondition = FilterSearchCondition.builder()
                .classKind("프리 다이빙")
                .organization(Organization.AIDA)
                .level("LEVEL1")
                .region("서울")
                .costCondition(costCondition)
                .build();

        Pageable pageable = PageRequest.of(0, 5);
        Page<Lecture> lecturePage = lectureJpaRepo.searchListByCondition(searchCondition, pageable);

        assertThat(lecturePage.getContent()).isNotEmpty();
        for (Lecture lecture : lecturePage.getContent()) {
            assertThat(lecture.getPrice()).isBetween(costCondition.getMin(), costCondition.getMax());
        }
    }

    @Test
    @DisplayName("14일 전부터 오늘까지 생성된 강의 목록 조회 - method name query 사용")
    public void getLecturesPrevious14() {
        saveLecturePerDate(LocalDateTime.now());
        saveLecturePerDate(LocalDateTime.now().minusDays(14));
        saveLecturePerDate(LocalDateTime.now().minusDays(15));

        LocalDateTime prevDateTime = LocalDateTime.now().minusDays(15);
        Pageable pageable = PageRequest.of(0, 20);

        Page<Lecture> lecturePage = lectureJpaRepo.findLectureByRegistrationDateAfter(prevDateTime, pageable);

        assertThat(lecturePage.getContent().size()).isEqualTo(10);
    }

    @Test
    @DisplayName("14일 전부터 오늘까지 생성된 강의 목록 조회 - query 직접 작성 사용")
    public void getLecturesPrevious14ByWritingQuery() {
        saveLecturePerDate(LocalDateTime.now());
        saveLecturePerDate(LocalDateTime.now().minusDays(14));
        saveLecturePerDate(LocalDateTime.now().minusDays(15));

        LocalDateTime prevDateTime = LocalDateTime.now().minusDays(15);
        Pageable pageable = PageRequest.of(0, 20);

        Page<Lecture> lecturePage = lectureJpaRepo.findFromPastDate(prevDateTime, pageable);

        assertThat(lecturePage.getContent().size()).isEqualTo(10);
    }

    public void saveLecturePerDate(LocalDateTime date) {
        for (int i = 0; i < 5; i++) {
            Lecture lecture = Lecture.builder()
                    .registrationDate(date)
                    .build();
            Lecture savedLecture = em.persist(lecture);

            for (int j = 0; j < 5; j++) {
                LectureImage lectureImage = LectureImage.builder()
                        .fileURI("Uri" + j)
                        .lecture(savedLecture)
                        .build();
                em.persist(lectureImage);
            }
        }

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("후기 갯수가 많은 순으로 인기 강의 목록 조회")
    public void findPopularLecturesOrderByReviewCount() {
        saveLectureWithReviewCount();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Lecture> lecturePage = lectureJpaRepo.findPopularLectures(pageable);

        List<Lecture> lectures = lecturePage.getContent();
        assertThat(lectures.get(0).getReviewCount()).isEqualTo(4);
    }

    public void saveLectureWithReviewCount(){
        for (int i = 0; i < 5; i++) {
            Lecture lecture = Lecture.builder()
                    .reviewCount(i)
                    .reviewTotalAvg(2.5f)
                    .build();
            em.persist(lecture);
        }
    }

    @Test
    @DisplayName("후기 갯수가 동일할 때 후기 총 평점이 높은 순으로 인기 강의 목록 조회")
    public void findPopularLecturesOrderByReviewTotalAvg() {
        saveLectureWithReviewTotalAvg();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Lecture> lecturePage = lectureJpaRepo.findPopularLectures(pageable);

        List<Lecture> lectures = lecturePage.getContent();
        assertThat(lectures.get(0).getReviewTotalAvg()).isEqualTo(4f);
    }

    public void saveLectureWithReviewTotalAvg(){
        for (int i = 0; i < 5; i++) {
            Lecture lecture = Lecture.builder()
                    .reviewCount(10)
                    .reviewTotalAvg((float) i)
                    .build();
            em.persist(lecture);
        }
    }
}