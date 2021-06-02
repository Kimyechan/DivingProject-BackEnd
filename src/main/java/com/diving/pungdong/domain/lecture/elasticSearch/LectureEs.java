package com.diving.pungdong.domain.lecture.elasticSearch;

import com.diving.pungdong.domain.lecture.Organization;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.Id;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder
@Document(indexName = "lecture")
public class LectureEs {
    @Id
    private Long id;
    private String nickName;
    private String title;
    private Organization organization;
    private String level;
    private String region;
    private Integer maxNumber;

    @Field(type = FieldType.Date, format = DateFormat.basic_time)
    private LocalTime lectureTime;

    private String imageUrl;
    private Integer price;
    private List<String> equipmentNames;
    private Float starAvg;
    private Integer reviewCount;
}