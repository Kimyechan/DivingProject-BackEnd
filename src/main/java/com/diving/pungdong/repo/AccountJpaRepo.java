package com.diving.pungdong.repo;

import com.diving.pungdong.domain.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountJpaRepo extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);
}
