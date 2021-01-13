package com.diving.pungdong.repo;

import com.diving.pungdong.advice.exception.CEmailSigninFailedException;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
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

    @Test
    @DisplayName("이메일로 단건 조회")
    public void findByEmail() {
        Account account = Account.builder()
                .email("rrr@gmail.com")
                .password("1234")
                .userName("rrr")
                .age(24)
                .gender(Gender.MALE)
                .roles(Sets.newLinkedHashSet(Role.STUDENT))
                .build();

        Account savedAccount = accountJpaRepo.save(account);

        Account findAccount = accountJpaRepo.findByEmail(savedAccount.getEmail()).orElseThrow(CEmailSigninFailedException::new);

        assertThat(findAccount.getEmail()).isEqualTo(savedAccount.getEmail());
    }

    @Test
    @DisplayName("수정")
    public void update() {
        Account account = Account.builder()
                .email("rrr@gmail.com")
                .password("1234")
                .userName("rrr")
                .age(24)
                .gender(Gender.MALE)
                .roles(Sets.newLinkedHashSet(Role.STUDENT))
                .build();

        Account savedAccount = accountJpaRepo.save(account);

        savedAccount.getRoles().add(Role.INSTRUCTOR);
        Account updatedAccount = accountJpaRepo.save(savedAccount);

        assertThat(updatedAccount.getRoles()).isEqualTo(savedAccount.getRoles());
    }
}