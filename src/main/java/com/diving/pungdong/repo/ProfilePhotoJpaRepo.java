package com.diving.pungdong.repo;

import com.diving.pungdong.domain.account.ProfilePhoto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfilePhotoJpaRepo extends JpaRepository<ProfilePhoto, Long> {
}
