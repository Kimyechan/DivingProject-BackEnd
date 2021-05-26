package com.diving.pungdong.repo.lecture;

import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.dto.lecture.list.search.FilterSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LectureJpaRepoCustom {
    Page<Lecture> searchListByCondition(FilterSearchCondition filterSearchCondition, Pageable pageable);
}
