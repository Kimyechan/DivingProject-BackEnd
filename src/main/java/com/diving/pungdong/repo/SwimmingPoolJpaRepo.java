package com.diving.pungdong.repo;

import com.diving.pungdong.domain.Location;
import com.diving.pungdong.domain.swimmingPool.SwimmingPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SwimmingPoolJpaRepo extends JpaRepository<SwimmingPool, Long> {
        Optional<SwimmingPool> findByLocation(Location location);
}
