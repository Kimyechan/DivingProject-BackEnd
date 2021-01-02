package com.diving.pungdong.repo;

import com.diving.pungdong.domain.swimmingPool.SwimmingPool;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SwimmingPoolJpaRepo extends JpaRepository<SwimmingPool, Long> {
}
