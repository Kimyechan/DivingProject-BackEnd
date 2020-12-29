package com.diving.pungdong.repo;

import com.diving.pungdong.domain.Token;
import org.springframework.data.repository.CrudRepository;

public interface TokenRedisRepo extends CrudRepository<Token, Long> {
}
