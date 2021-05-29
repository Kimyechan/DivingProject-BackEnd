package com.diving.pungdong.service;

import com.diving.pungdong.config.security.JwtTokenProvider;
import com.diving.pungdong.controller.sign.SignController.AddInstructorRoleReq;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.repo.AccountJpaRepo;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
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
    private InstructorImageService instructorImageService;

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

    @Test
    @DisplayName("강사로 수정, 강사관련 이미지 저장")
    public void updateAccountToInstructor() throws IOException {
        Account account = Account.builder()
                .email("rrr@gmail.com")
                .password("1234")
                .nickName("yechan")
                .birth("1999-09-11")
                .gender(Gender.MALE)
                .roles(Sets.newLinkedHashSet(Role.STUDENT))
                .build();

        given(accountJpaRepo.findByEmail(account.getEmail())).willReturn(java.util.Optional.of(account));
        AddInstructorRoleReq request = AddInstructorRoleReq.builder()
                .phoneNumber("11122223333")
                .groupName("AIDA")
                .description("강사 설명")
                .build();
        account.setPhoneNumber(request.getPhoneNumber());
        account.setGroupName(request.getGroupName());
        account.setDescription(request.getDescription());
        account.getRoles().add(Role.INSTRUCTOR);

        given(accountJpaRepo.save(account)).willReturn(account);

        Account updatedAccount = accountService.updateAccountToInstructor(account.getEmail(), request, new ArrayList<>(), new ArrayList<>());

        assertThat(updatedAccount.getRoles()).isEqualTo(Set.of(Role.INSTRUCTOR, Role.STUDENT));
    }
}