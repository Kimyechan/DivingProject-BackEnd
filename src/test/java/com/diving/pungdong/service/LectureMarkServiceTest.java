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
import java.util.Optional;

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

    @Test
    @DisplayName("회원 좋아요 체크 조회 - 로그인 안 했을 때")
    public void existLectureMarkNotLogin() {
        //given
        Long lectureId = 1L;

        // when
        boolean marked = lectureMarkService.existLectureMark(null, lectureId);

        // then
        assertFalse(marked);
    }

    @Test
    @DisplayName("회원 좋아요 체크 안 했을 때 여부 조회")
    public void existLectureMarkFalse() {
        //given
        Account account = Account.builder().id(1L).build();
        Long lectureId = 1L;

        given(lectureMarkJpaRepo.findByAccountAndLecture(account.getId(), lectureId)).willReturn(Optional.empty());

        // when
        boolean marked = lectureMarkService.existLectureMark(account, lectureId);

        // then
        assertFalse(marked);
    }

    @Test
    @DisplayName("회원 좋아요 체크 했을 때 여부 조회")
    public void existLectureMarkTrue() {
        //given
        Account account = Account.builder().id(1L).build();
        Long lectureId = 1L;

        given(lectureMarkJpaRepo.findByAccountAndLecture(account.getId(), lectureId)).willReturn(Optional.of(new LectureMark()));

        // when
        boolean marked = lectureMarkService.existLectureMark(account, lectureId);

        // then
        assertTrue(marked);
    }
}