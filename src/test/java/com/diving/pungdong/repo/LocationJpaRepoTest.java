package com.diving.pungdong.repo;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.location.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
class LocationJpaRepoTest {
    @Autowired
    private LocationJpaRepo locationJpaRepo;

    @Autowired
    private TestEntityManager em;

    private Long createLectureLocation() {
        Location location = Location.builder()
                .address("상세 주소")
                .latitude(35.111)
                .longitude(126.5645)
                .build();
        em.persist(location);

        Lecture lecture = Lecture.builder()
                .location(location)
                .build();
        em.persist(lecture);

        em.flush();
        em.clear();

        return lecture.getId();
    }

    @Test
    @DisplayName("강의 Id로 강의 위치 조회")
    public void findByLectureId() {
        Long lectureId = createLectureLocation();

        Location location = locationJpaRepo.findByLectureId(lectureId).orElseThrow(BadRequestException::new);

        assertThat(location).isNotNull();
    }
}