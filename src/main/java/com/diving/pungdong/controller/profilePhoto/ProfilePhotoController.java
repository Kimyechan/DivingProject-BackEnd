package com.diving.pungdong.controller.profilePhoto;

import com.diving.pungdong.config.security.CurrentUser;
import com.diving.pungdong.controller.lecture.LectureController;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.dto.lecture.delete.LectureDeleteRes;
import com.diving.pungdong.dto.profilePhoto.ProfilePhotoUpdateInfo;
import com.diving.pungdong.service.account.ProfilePhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile-photo")
public class ProfilePhotoController {
    private final ProfilePhotoService profilePhotoService;

    @PostMapping
    public ResponseEntity<?> modifyProfilePhoto(@CurrentUser Account account,
                                                @RequestParam("image") MultipartFile image) throws IOException {
        ProfilePhotoUpdateInfo updateInfo = profilePhotoService.updateProfilePhoto(account, image);

        EntityModel<ProfilePhotoUpdateInfo> model = EntityModel.of(updateInfo);
        model.add(Link.of("/docs/api.html#resource-profilePhoto-update").withRel("profile"));
        return ResponseEntity.ok().body(model);
    }
}
