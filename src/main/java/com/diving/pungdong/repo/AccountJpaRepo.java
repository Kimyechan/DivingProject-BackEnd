package com.diving.pungdong.repo;

import com.diving.pungdong.domain.account.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AccountJpaRepo extends JpaRepository<Account, Long> {
    Boolean existsByEmail(String email);

    Optional<Account> findByEmail(String email);

    Optional<Account> findByNickName(String nickName);

    @Query("select a from Account a where a.isRequestCertified = true and a.isCertified = false")
    Page<Account> findAllRequestInstructor(Pageable pageable);
}
