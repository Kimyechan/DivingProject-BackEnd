package com.diving.pungdong.service.kafka;

import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.dto.account.kafka.AccountInfo;
import com.diving.pungdong.dto.account.kafka.FirebaseTokenInfo;
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

    public void sendFirebaseTokenInfo(String id, String token) {
        FirebaseTokenInfo firebaseTokenInfo = FirebaseTokenInfo.builder()
                .id(id)
                .token(token)
                .build();

        this.kafkaTemplate.send("firebase-token", firebaseTokenInfo);
    }

}
