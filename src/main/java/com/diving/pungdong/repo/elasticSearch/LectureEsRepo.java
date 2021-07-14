package com.diving.pungdong.repo.elasticSearch;

import com.diving.pungdong.domain.lecture.elasticSearch.LectureEs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LectureEsRepo extends ElasticsearchRepository<LectureEs, String> {
    @Query("{\n" +
            "\t\"bool\": {\n" +
            "\t      \"should\": [\n" +
            "\t        { \"match\": \n" +
            "\t\t\t\t\t\t\t{ \n" +
            "\t\t\t\t\t\t\t\t\"title\": {\n" +
            "\t\t\t\t\t\t\t\t\t\"query\" : \"?0\",\n" +
            "\t\t\t\t\t\t\t\t\t\"operator\" : \"and\"\n" +
            "\t\t\t\t\t\t\t} \n" +
            "\t\t\t\t\t},\n" +
            "\t        { \"match\": \n" +
            "\t\t\t\t\t\t\t{ \n" +
            "\t\t\t\t\t\t\t\t\"nickName\": {\n" +
            "\t\t\t\t\t\t\t\t\t\"query\" : \"?1\",\n" +
            "\t\t\t\t\t\t\t\t\t\"operator\" : \"and\"\n" +
            "\t\t\t\t\t\t\t}  \n" +
            "\t\t\t\t\t}\n" +
            "\t      ]\n" +
            "\t    }\n" +
            "}")
    Page<LectureEs> findByTitleOrNickName(String title, String nickName, Pageable pageable);

    List<LectureEs> findAllByNickName(String nickName);
}
