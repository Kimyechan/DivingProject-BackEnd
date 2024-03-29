package com.diving.pungdong.service.kafka;

import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.service.kafka.dto.account.AccountInfo;
import com.diving.pungdong.service.kafka.dto.account.FirebaseTokenInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountKafkaProducer {
    private final KafkaTemplate<Object, Object> kafkaTemplate;

    public void sendAccountInfo(Account account) {
        AccountInfo accountInfo = AccountInfo.builder()
                .id(String.valueOf(account.getId()))
                .password(account.getPassword())
                .roles(account.getRoles())
                .nickName(account.getNickName())
                .profileImageUrl(account.getProfilePhoto().getImageUrl())
                .build();

        this.kafkaTemplate.send("account", accountInfo);
    }

    public void sendAccountUpdateInfo(Account account) {
        AccountInfo accountInfo = AccountInfo.builder()
                .id(String.valueOf(account.getId()))
                .password(account.getPassword())
                .roles(account.getRoles())
                .nickName(account.getNickName())
                .profileImageUrl(account.getProfilePhoto().getImageUrl())
                .build();

        this.kafkaTemplate.send("update-account", accountInfo);
    }

    public void sendFirebaseTokenInfo(String id, String token) {
        FirebaseTokenInfo firebaseTokenInfo = FirebaseTokenInfo.builder()
                .id(id)
                .token(token)
                .build();

        this.kafkaTemplate.send("firebase-token", firebaseTokenInfo);
    }

}
