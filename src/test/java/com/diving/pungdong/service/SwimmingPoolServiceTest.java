package com.diving.pungdong.service;

import com.diving.pungdong.domain.swimmingPool.Location;
import com.diving.pungdong.domain.swimmingPool.SwimmingPool;
import com.diving.pungdong.repo.SwimmingPoolJpaRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ActiveProfiles("test")
@Transactional
class SwimmingPoolServiceTest {

    private SwimmingPoolService swimmingPoolService;

    @Mock
    private SwimmingPoolJpaRepo swimmingPoolJpaRepo;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        swimmingPoolService = new SwimmingPoolService(swimmingPoolJpaRepo);
    }

    @Test
    @DisplayName("수영장 단건 조회 By Id")
    public void getSwimmingPool() {
        Location location = new Location(10.0, 10.0);
        SwimmingPool swimmingPool = SwimmingPool.builder()
                .id(1L)
                .location(location)
                .build();
        given(swimmingPoolJpaRepo.findById(1L)).willReturn(Optional.of(swimmingPool));

        SwimmingPool findSwimmingPool = swimmingPoolService.getSwimmingPool(1L);

        assertThat(findSwimmingPool.getId()).isEqualTo(swimmingPool.getId());
    }
}