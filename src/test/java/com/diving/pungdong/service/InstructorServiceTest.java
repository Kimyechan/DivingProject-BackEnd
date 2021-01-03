package com.diving.pungdong.service;

import com.diving.pungdong.advice.exception.CEmailSigninFailedException;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Instructor;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.repo.InstructorJpaRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ActiveProfiles("test")
@Transactional
class InstructorServiceTest {

    private InstructorService instructorService;

    @Mock
    private InstructorJpaRepo instructorJpaRepo;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        instructorService = new InstructorService(instructorJpaRepo);
    }

    @Test
    @DisplayName("강사 생성")
    public void saveInstructor() {
        Instructor instructor = Instructor.builder()
                .email("rrr@gmail.com")
                .password("1234")
                .userName("rrr")
                .age(24)
                .gender(Gender.MALE)
                .roles(Set.of(Role.INSTRUCTOR))
                .build();

        Instructor savedInstructor = Instructor.builder()
                .id(1L)
                .email("rrr@gmail.com")
                .password("1234")
                .userName("rrr")
                .age(24)
                .gender(Gender.MALE)
                .roles(Set.of(Role.INSTRUCTOR))
                .build();

        given(instructorJpaRepo.save(instructor)).willReturn(savedInstructor);
        Instructor returnedInstructor = instructorService.saveInstructor(instructor);

        assertThat(returnedInstructor.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("강사 단건 조회 By Email")
    public void getInstructorByEmail() {
        Instructor savedInstructor = Instructor.builder()
                .id(1L)
                .email("rrr@gmail.com")
                .password("1234")
                .userName("rrr")
                .age(24)
                .gender(Gender.MALE)
                .roles(Set.of(Role.INSTRUCTOR))
                .build();

        String email = "rrr@gmail.com";
        given(instructorJpaRepo.findByEmail(email)).willReturn(Optional.of(savedInstructor));

        Instructor returnedInstructor = instructorService.getInstructorByEmail(email);

        assertThat(returnedInstructor.getEmail()).isEqualTo(savedInstructor.getEmail());
    }

    @Test
    @DisplayName("강사 단건 조회 By Email 이메일 없음")
    public void getInstructorByEmailNotMatch() {
        String email = "rrr@gmail.com";
        given(instructorJpaRepo.findByEmail(email)).willThrow(CEmailSigninFailedException.class);

        assertThrows(CEmailSigninFailedException.class, () -> instructorService.getInstructorByEmail(email));
    }
}