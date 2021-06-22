package com.diving.pungdong.controller.profilePhoto;

import com.diving.pungdong.config.security.CurrentUser;
import com.diving.pungdong.controller.lecture.LectureController;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.dto.lecture.delete.LectureDeleteRes;
import com.diving.pungdong.dto.profilePhoto.ProfilePhotoInfo;
import com.diving.pungdong.dto.profilePhoto.ProfilePhotoUpdateInfo;
import com.diving.pungdong.service.account.ProfilePhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile-photo")
public class ProfilePhotoController {
    private final ProfilePhotoService profilePhotoService;

    @GetMapping
    public ResponseEntity<?> readProfilePhoto(@CurrentUser Account account) {
        ProfilePhotoInfo profilePhotoInfo = profilePhotoService.findByAccount(account);

        EntityModel<ProfilePhotoInfo> model = EntityModel.of(profilePhotoInfo);
        model.add(linkTo(methodOn(ProfilePhotoController.class).readProfilePhoto(account)).withSelfRel());
        model.add(Link.of("/docs/api.html#resource-profilePhoto-update").withRel("profile"));

        return ResponseEntity.ok().body(model);
    }

    @PostMapping
    public ResponseEntity<?> modifyProfilePhoto(@CurrentUser Account account,
                                                @RequestParam("image") MultipartFile image) throws IOException {
        ProfilePhotoUpdateInfo updateInfo = profilePhotoService.updateProfilePhoto(account, image);

        EntityModel<ProfilePhotoUpdateInfo> model = EntityModel.of(updateInfo);
        model.add(Link.of("/docs/api.html#resource-profilePhoto-update").withRel("profile"));
        return ResponseEntity.ok().body(model);
    }
}
