package com.diving.pungdong.service;

import com.diving.pungdong.domain.LectureMark;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.repo.LectureMarkJpaRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class LectureMarkServiceTest {
    @InjectMocks
    private LectureMarkService lectureMarkService;
    
    @Mock
    private LectureMarkJpaRepo lectureMarkJpaRepo;
    
    @Test
    @DisplayName("한 회원의 좋아요 목록 맵 조회")
    public void findLikeLectureMap() {
        Account account = Account.builder().id(1L).build();

        List<LectureMark> lectureMarks = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Lecture lecture = Lecture.builder()
                    .id((long) i)
                    .build();   
            
            LectureMark lectureMark = LectureMark.builder()
                    .id((long) i)
                    .account(account)
                    .lecture(lecture)
                    .build();
            
            lectureMarks.add(lectureMark);
        }
        
        given(lectureMarkJpaRepo.findByAccount(account)).willReturn(lectureMarks);

        Map<Long, Boolean> likeLectureMap = lectureMarkService.findLikeLectureMap(account);

        for (LectureMark lectureMark : lectureMarks) {
            Long lectureId = lectureMark.getLecture().getId();

            assertThat(likeLectureMap.getOrDefault(lectureId, false)).isEqualTo(true);
        }
    }
}