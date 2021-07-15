package com.diving.pungdong.controller.lectureImage;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.config.security.CurrentUser;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.dto.lectureImage.LectureImageInfo;
import com.diving.pungdong.dto.lectureImage.LectureImageUrl;
import com.diving.pungdong.dto.lectureImage.delete.LectureImageDeleteInfo;
import com.diving.pungdong.service.LectureImageService;
import com.diving.pungdong.service.elasticSearch.LectureEsService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/lectureImage")
public class LectureImageController {
    private final LectureImageService lectureImageService;
    private final LectureEsService lectureEsService;

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

    @GetMapping("/list")
    public ResponseEntity<?> findLectureImages(@Valid @RequestParam Long lectureId) {
        List<LectureImageUrl> lectureImages = lectureImageService.findLectureImagesUrl(lectureId);

        CollectionModel<LectureImageUrl> model = CollectionModel.of(lectureImages);
        model.add(linkTo(methodOn(LectureImageController.class).findLectureImages(lectureId)).withSelfRel());
        model.add(Link.of("/docs/api.html#resource-lectureImage-find-list").withRel("profile"));
        return ResponseEntity.ok().body(model);
    }

    @DeleteMapping("/list")
    public ResponseEntity<?> deleteLectureImages(@CurrentUser Account account,
                                                 @Valid @RequestBody LectureImageDeleteInfo lectureImageDeleteInfo,
                                                 BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException();
        }

        lectureImageService.deleteImages(account, lectureImageDeleteInfo);
        lectureEsService.updateMainLectureImage(lectureImageDeleteInfo.getLectureId());

        return ResponseEntity.noContent().build();
    }
}
