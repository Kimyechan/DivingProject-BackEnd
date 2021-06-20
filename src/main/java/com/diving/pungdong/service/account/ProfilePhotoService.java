package com.diving.pungdong.service.account;

import com.diving.pungdong.advice.exception.ResourceNotFoundException;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.ProfilePhoto;
import com.diving.pungdong.dto.profilePhoto.ProfilePhotoUpdateInfo;
import com.diving.pungdong.repo.ProfilePhotoJpaRepo;
import com.diving.pungdong.service.image.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ProfilePhotoService {
    private final ProfilePhotoJpaRepo profilePhotoJpaRepo;
    private final S3Uploader s3Uploader;

    @Transactional
    public ProfilePhoto saveDefaultProfilePhoto() {
        ProfilePhoto profilePhoto = ProfilePhoto.builder()
                .imageUrl("vlvkcjswo71@gmail.com2021-06-07T18:08:34.039977.png")
                .build();

        return profilePhotoJpaRepo.save(profilePhoto);
    }

    @Transactional(readOnly = true)
    public ProfilePhoto findByProfilePhotoId(Long profilePhotoId) {
        return profilePhotoJpaRepo.findById(profilePhotoId).orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional
    public ProfilePhotoUpdateInfo updateProfilePhoto(Account account, MultipartFile image) throws IOException {
        ProfilePhoto profilePhoto = findByProfilePhotoId(account.getProfilePhoto().getId());

        String fileUri = s3Uploader.upload(image, "profile-photo", account.getEmail());
        profilePhoto.setImageUrl(fileUri);

        return ProfilePhotoUpdateInfo.builder()
                .profilePhotoId(profilePhoto.getId())
                .url(profilePhoto.getImageUrl())
                .build();
    }
}
