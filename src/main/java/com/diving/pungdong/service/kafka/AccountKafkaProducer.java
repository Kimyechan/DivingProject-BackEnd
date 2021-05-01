package com.diving.pungdong.service.kafka;

import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.dto.account.AccountInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AccountKafkaProducer {
    private final KafkaTemplate<Object, Object> kafkaTemplate;

    public void sendAccountInfo(String id, String password, Set<Role> roles) {
        AccountInfo accountInfo = AccountInfo.builder()
                .id(id)
                .password(password)
                .roles(roles)
                .build();

        this.kafkaTemplate.send("account", accountInfo);
    }
}
