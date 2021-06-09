package com.diving.pungdong.repo;

import com.diving.pungdong.domain.lecture.LectureImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LectureImageJpaRepo extends JpaRepository<LectureImage, Long> {
    void deleteByFileURI(String fileURL);

    @Query("select li from LectureImage li where li.lecture.id = :lectureId")
    List<LectureImage> findAllByLectureId(@Param("lectureId") Long lectureId);
}
