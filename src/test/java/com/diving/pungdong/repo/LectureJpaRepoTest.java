package com.diving.pungdong.repo;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.LectureImage;
import com.diving.pungdong.dto.lecture.search.CostCondition;
import com.diving.pungdong.dto.lecture.search.SearchCondition;
import com.diving.pungdong.repo.lecture.LectureJpaRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LectureJpaRepoTest {
    @Autowired
    private LectureJpaRepo lectureJpaRepo;

    @Autowired
    private LectureImageJpaRepo lectureImageJpaReop;

    @Test
    @DisplayName("강의 저장")
    public void save() {
        Lecture lecture = Lecture.builder()
                .title("강의1")
                .description("내용1")
                .classKind("스쿠버 다이빙")
                .groupName("AIDA")
                .certificateKind("Level1")
                .price(100000)
                .instructor(new Account())
                .lectureImages(List.of(new LectureImage()))
                .build();

        Lecture savedLecture = lectureJpaRepo.save(lecture);

        assertThat(savedLecture.getId()).isNotNull();
    }

    @Transactional
    public void createLecture(String certificateKind, String region) {
        for (int i = 0; i < 15; i++) {
            Lecture lecture = Lecture.builder()
                    .groupName("AIDA")
                    .title("강의")
                    .description("내용")
                    .classKind("스쿠버 다이빙")
                    .groupName("AIDA")
                    .certificateKind(certificateKind)
                    .price(100000 * (i + 1))
                    .region(region)
                    .build();

            lectureJpaRepo.save(lecture);

            for (int j = 0; j < 2; j++) {
                LectureImage lectureImage = LectureImage.builder()
                        .fileURI("Image URL 주소")
                        .lecture(lecture)
                        .build();
                lecture.getLectureImages().add(lectureImage);
                lectureImageJpaReop.save(lectureImage);
            }
        }
    }

    @Test
    @DisplayName("강의 리스트 조회 - 단체 종류")
    public void searchListByGroupName() {
        createLecture("LEVEL1", "서울");
        SearchCondition searchCondition = SearchCondition.builder()
                .groupName("AIDA")
                .build();

        Pageable pageable = PageRequest.of(0, 5);
        Page<Lecture> lecturePage = lectureJpaRepo.searchListByCondition(searchCondition, pageable);

        assertThat(lecturePage.getContent()).isNotEmpty();
        for (Lecture lecture : lecturePage.getContent()) {
            assertThat(lecture.getGroupName()).isEqualTo("AIDA");
        }
    }

    @Test
    @DisplayName("강의 리스트 조회 - 단체 종류")
    public void searchListByGroupNameFail() {
        createLecture("LEVEL1", "서울");
        SearchCondition searchCondition = SearchCondition.builder()
                .groupName("PADI")
                .build();

        Pageable pageable = PageRequest.of(0, 5);
        Page<Lecture> lecturePage = lectureJpaRepo.searchListByCondition(searchCondition, pageable);

        assertThat(lecturePage.getContent().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("강의 리스트 조회 - 자격증 종류")
    public void searchListByCertificateKind() {
        createLecture("LEVEL1", "서울");
        SearchCondition searchCondition = SearchCondition.builder()
                .certificateKind("LEVEL1")
                .build();
        Pageable pageable = PageRequest.of(0, 5);
        Page<Lecture> lecturePage = lectureJpaRepo.searchListByCondition(searchCondition, pageable);

        assertThat(lecturePage.getContent()).isNotEmpty();
        for (Lecture lecture : lecturePage.getContent()) {
            assertThat(lecture.getCertificateKind()).isEqualTo("LEVEL1");
        }
    }

    @Test
    @DisplayName("강의 리스트 조회시 결과 없음 - 자격증 종류")
    public void searchListByCertificateKindFail() {
        createLecture("LEVEL1", "서울");
        SearchCondition searchCondition = SearchCondition.builder()
                .certificateKind("LEVEL2")
                .build();
        Pageable pageable = PageRequest.of(0, 5);
        Page<Lecture> lecturePage = lectureJpaRepo.searchListByCondition(searchCondition, pageable);

        assertThat(lecturePage.getContent().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("강의 리스트 조회 - 지역별")
    public void searchListByRegion() {
        createLecture("LEVEL1", "서울");
        SearchCondition searchCondition = SearchCondition.builder()
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
        createLecture("LEVEL1", "서울");
        SearchCondition searchCondition = SearchCondition.builder()
                .region("부산")
                .build();
        Pageable pageable = PageRequest.of(0, 5);
        Page<Lecture> lecturePage = lectureJpaRepo.searchListByCondition(searchCondition, pageable);

        assertThat(lecturePage.getContent().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("강의 리스트 조회 - 강의료")
    public void searchListByCost() {
        createLecture("LEVEL1", "서울");
        CostCondition costCondition = CostCondition.builder()
                .max(150000)
                .min(10000)
                .build();

        SearchCondition searchCondition = SearchCondition.builder()
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
        createLecture("LEVEL1", "서울");
        CostCondition costCondition = CostCondition.builder()
                .max(10000)
                .min(5000)
                .build();

        SearchCondition searchCondition = SearchCondition.builder()
                .costCondition(costCondition)
                .build();

        Pageable pageable = PageRequest.of(0, 5);
        Page<Lecture> lecturePage = lectureJpaRepo.searchListByCondition(searchCondition, pageable);

        assertThat(lecturePage.getContent()).isEmpty();
    }

    @Test
    @DisplayName("강의 리스트 조회 - 전체 조건")
    public void searchListByAll() {
        createLecture("LEVEL1", "서울");
        CostCondition costCondition = CostCondition.builder()
                .max(150000)
                .min(10000)
                .build();

        SearchCondition searchCondition = SearchCondition.builder()
                .certificateKind("LEVEL1")
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
}