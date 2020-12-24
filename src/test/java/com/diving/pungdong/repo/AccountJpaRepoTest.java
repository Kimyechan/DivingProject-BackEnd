package com.diving.pungdong.repo;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AccountJpaRepoTest {
    @Autowired
    private AccountJpaRepo accountJpaRepo;

    @Test
    public void save() {
        Account account = Account.builder()
                .email("rrr@gmail.com")
                .password("1234")
                .userName("rrr")
                .age(24)
                .gender(Gender.MALE)
                .roles(Set.of(Role.INSTRUCTOR))
                .build();

        Account savedAccount = accountJpaRepo.save(account);

        assertThat(savedAccount.getId()).isNotNull();
    }
}