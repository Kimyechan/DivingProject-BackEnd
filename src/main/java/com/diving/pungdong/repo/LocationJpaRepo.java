package com.diving.pungdong.repo;

import com.diving.pungdong.domain.location.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationJpaRepo extends JpaRepository<Location, Long> {
}
