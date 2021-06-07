package com.diving.pungdong.service.account;

import com.diving.pungdong.domain.account.ProfilePhoto;
import com.diving.pungdong.repo.ProfilePhotoJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfilePhotoService {
    private final ProfilePhotoJpaRepo profilePhotoJpaRepo;

    @Transactional
    public ProfilePhoto saveDefaultProfilePhoto() {
        ProfilePhoto profilePhoto = ProfilePhoto.builder()
                .imageUrl("vlvkcjswo71@gmail.com2021-06-07T18:08:34.039977.png")
                .build();

        return profilePhotoJpaRepo.save(profilePhoto);
    }
}
