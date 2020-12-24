package com.diving.pungdong.service;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.repo.AccountJpaRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

class AccountServiceTest {
    private AccountService accountService;

    @Mock
    private AccountJpaRepo accountJpaRepo;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        accountService = new AccountService(accountJpaRepo);
    }

    @Test
    @DisplayName("account 계정을 저장한다")
    public void saveAccount() {
        Account account = Account.builder()
                .email("rrr@gmail.com")
                .password("1234")
                .userName("rrr")
                .age(24)
                .gender(Gender.MALE)
                .roles(Set.of(Role.INSTRUCTOR))
                .build();

        Account returnedAccount = Account.builder()
                .id(any())
                .email(account.getEmail())
                .password(account.getPassword())
                .userName(account.getUserName())
                .age(account.getAge())
                .gender(account.getGender())
                .roles(account.getRoles())
                .build();

        given(accountJpaRepo.save(account)).willReturn(returnedAccount);

        Account savedAccount = accountService.saveAccount(account);
        assertThat(savedAccount).isNotNull();
    }


}