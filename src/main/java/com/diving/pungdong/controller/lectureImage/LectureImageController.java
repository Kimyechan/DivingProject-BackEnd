package com.diving.pungdong.controller.lectureImage;

import com.diving.pungdong.config.security.CurrentUser;
import com.diving.pungdong.controller.lecture.LectureController;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.dto.lectureImage.LectureImageInfo;
import com.diving.pungdong.service.LectureImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/lectureImage")
public class LectureImageController {
    private final LectureImageService lectureImageService;

    @PostMapping("/create/list")
    public ResponseEntity<?> createLectureImages(@CurrentUser Account account,
                                                 @RequestParam("lectureId") Long lectureId,
                                                 @RequestParam("images") List<MultipartFile> images) throws IOException {
        LectureImageInfo lectureImageInfo = lectureImageService.saveImages(lectureId, account, images);

        EntityModel<LectureImageInfo> model = EntityModel.of(lectureImageInfo);
        WebMvcLinkBuilder location = linkTo(methodOn(LectureImageController.class).createLectureImages(account, lectureId, images));
        model.add(location.withSelfRel());
        model.add(Link.of("/docs/api.html#resource-lecture-images-create").withRel("profile"));

        return ResponseEntity.created(location.toUri()).body(model);
    }
}
