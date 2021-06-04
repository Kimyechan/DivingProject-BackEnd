package com.diving.pungdong.service;

import com.diving.pungdong.config.security.JwtTokenProvider;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.repo.AccountJpaRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @InjectMocks
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
}