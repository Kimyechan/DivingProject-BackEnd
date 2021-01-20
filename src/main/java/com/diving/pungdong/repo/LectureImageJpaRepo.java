package com.diving.pungdong.repo;

import com.diving.pungdong.domain.lecture.LectureImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureImageJpaRepo extends JpaRepository<LectureImage, Long> {
    void deleteByFileURI(String fileURL);
}
