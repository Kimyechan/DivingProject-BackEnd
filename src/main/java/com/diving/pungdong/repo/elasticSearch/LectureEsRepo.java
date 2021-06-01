package com.diving.pungdong.repo.elasticSearch;

import com.diving.pungdong.domain.lecture.elasticSearch.LectureEs;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface LectureEsRepo extends ElasticsearchRepository<LectureEs, String> {
    List<LectureEs> findByTitle(String title);
}
