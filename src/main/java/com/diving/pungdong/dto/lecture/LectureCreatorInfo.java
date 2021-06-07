package com.diving.pungdong.dto.lecture;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureCreatorInfo {
    private Long instructorId;
    private String nickName;
    private String selfIntroduction;
    private String profilePhotoUrl;
}
