package com.diving.pungdong.service;

import com.diving.pungdong.config.security.JwtTokenProvider;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.dto.account.update.ForgotPasswordInfo;
import com.diving.pungdong.repo.AccountJpaRepo;
import com.diving.pungdong.service.account.AccountService;
import com.diving.pungdong.service.kafka.AccountKafkaProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @InjectMocks
    @Spy
    private AccountService accountService;

    @Mock
    private AccountJpaRepo accountJpaRepo;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private  RedisTemplate<String, String> redisTemplate;

    @Mock
    private InstructorCertificateService instructorCertificateService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccountKafkaProducer accountKafkaProducer;

    @Mock
    private EmailService emailService;

    @Test
    @DisplayName("account 계정을 저장한다")
    public void saveAccount() {
        Account account = Account.builder()
                .email("rrr@gmail.com")
                .password("1234")
                .nickName("yechan")
                .birth("1999-09-11")
                .gender(Gender.MALE)
                .roles(Set.of(Role.INSTRUCTOR))
                .build();

        Account returnedAccount = Account.builder()
                .id(any())
                .email(account.getEmail())
                .password(account.getPassword())
                .nickName(account.getNickName())
                .birth(account.getBirth())
                .gender(account.getGender())
                .roles(account.getRoles())
                .build();

        given(accountJpaRepo.save(account)).willReturn(returnedAccount);

        Account savedAccount = accountService.saveAccount(account);
        assertThat(savedAccount).isNotNull();
    }

    @Test
    public void userDetailService() {
        Account account = Account.builder()
                .id(1L)
                .email("yechan@gmail.com")
                .password("1234")
                .roles(Set.of(Role.INSTRUCTOR))
                .build();

        given(accountJpaRepo.findById(account.getId())).willReturn(java.util.Optional.of(account));

        UserDetails userDetails = accountService.loadUserByUsername(String.valueOf(account.getId()));
        assertThat(userDetails.getUsername()).isEqualTo("1");
    }

    @Test
    @DisplayName("강사 권한 추가")
    public void addInstructorRole() {
        Set<Role> roles = new HashSet<>();
        roles.add(Role.STUDENT);
        Account account = Account.builder()
                .id(1L)
                .roles(roles)
                .build();

        doReturn(account).when(accountService).findAccountById(account.getId());

        Account instructor = accountService.addInstructorRole(account.getId());

        assertThat(instructor.getRoles()).contains(Role.INSTRUCTOR);
    }

    @Test
    @DisplayName("강사 지원 여부 확인")
    public void checkInstructorApplication() {
        // given
        Account account = Account.builder()
                .id(1L)
                .isRequestCertified(true)
                .build();

        doReturn(account).when(accountService).findAccountById(account.getId());

        // when
        boolean isApplied = accountService.checkInstructorApplication(account.getId());

        // then
        assertTrue(isApplied);
    }

    @Test
    @DisplayName("잊어버린 비밀번호 새로운 비밀번호로 변경")
    public void modifyForgetPassword() {
        // given
        ForgotPasswordInfo forgotPasswordInfo = ForgotPasswordInfo.builder()
                .email("abc1234@gmail.com")
                .newPassword("abcd")
                .authCode("34212")
                .build();


        Account account = Account.builder()
                .password(passwordEncoder.encode("1234"))
                .build();

        doReturn(account).when(accountService).findAccountByEmail(forgotPasswordInfo.getEmail());

        // when
        accountService.modifyForgetPassword(forgotPasswordInfo);

        // thenR
        assertThat(account.getPassword()).isEqualTo(passwordEncoder.encode("abcd"));
    }
}