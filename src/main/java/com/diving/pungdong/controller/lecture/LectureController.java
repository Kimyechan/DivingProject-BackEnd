package com.diving.pungdong.controller.lecture;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.advice.exception.NoPermissionsException;
import com.diving.pungdong.config.security.CurrentUser;
import com.diving.pungdong.domain.LectureMark;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.elasticSearch.LectureEs;
import com.diving.pungdong.dto.lecture.like.mark.MarkLectureInfo;
import com.diving.pungdong.dto.lecture.LectureCreatorInfo;
import com.diving.pungdong.dto.lecture.create.LectureCreateInfo;
import com.diving.pungdong.dto.lecture.create.LectureCreateResult;
import com.diving.pungdong.dto.lecture.delete.LectureDeleteRes;
import com.diving.pungdong.dto.lecture.detail.LectureDetail;
import com.diving.pungdong.dto.lecture.like.list.LikeLectureInfo;
import com.diving.pungdong.dto.lecture.like.mark.MarkLectureResult;
import com.diving.pungdong.dto.lecture.like.unmark.UnmarkLectureInfo;
import com.diving.pungdong.dto.lecture.list.LectureInfo;
import com.diving.pungdong.dto.lecture.list.mylist.MyLectureInfo;
import com.diving.pungdong.dto.lecture.list.newList.NewLectureInfo;
import com.diving.pungdong.dto.lecture.list.search.FilterSearchCondition;
import com.diving.pungdong.dto.lecture.update.LectureClosedInfo;
import com.diving.pungdong.dto.lecture.update.LectureUpdateInfo;
import com.diving.pungdong.dto.lectureMark.LectureMarkModel;
import com.diving.pungdong.service.LectureMarkService;
import com.diving.pungdong.service.LectureService;
import com.diving.pungdong.service.elasticSearch.LectureEsService;
import com.diving.pungdong.service.image.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/lecture")
public class LectureController {
    private final LectureService lectureService;
    private final LectureEsService lectureEsService;
    private final LectureMarkService lectureMarkService;
    private final S3Uploader s3Uploader;

    @PostMapping("/create")
    public ResponseEntity<?> createLecture(@CurrentUser Account account,
                                           @Valid @RequestBody LectureCreateInfo lectureCreateInfo,
                                           BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException();
        }

        LectureCreateResult lectureCreateResult = lectureService.createLecture(account, lectureCreateInfo);

        EntityModel<LectureCreateResult> model = EntityModel.of(lectureCreateResult);
        WebMvcLinkBuilder location = linkTo(methodOn(LectureController.class).createLecture(account, lectureCreateInfo, result));
        model.add(location.withSelfRel());
        model.add(Link.of("/docs/api.html#resource-lecture-create").withRel("profile"));

        return ResponseEntity.created(location.toUri()).body(model);
    }

    @PostMapping("/{id}/elastic-search")
    public ResponseEntity<?> createLectureEs(@CurrentUser Account account,
                                             @PathVariable("id") Long lectureId) {
        lectureEsService.saveLectureInfo(account, lectureId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping
    public ResponseEntity<?> updateLecture(@CurrentUser Account account,
                                           @Valid @RequestBody LectureUpdateInfo lectureUpdateInfo,
                                           BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException();
        }

        lectureService.updateLecture(lectureUpdateInfo, account);
        lectureEsService.updateLectureInfo(lectureUpdateInfo);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<EntityModel<LectureDeleteRes>> deleteLecture(@CurrentUser Account account, @RequestParam("id") Long id) {
        Lecture lecture = lectureService.findLectureById(id);
        if (!lecture.getInstructor().getEmail().equals(account.getEmail())) {
            throw new NoPermissionsException();
        }

        lectureService.deleteLectureById(id);

        LectureDeleteRes lectureDeleteRes = LectureDeleteRes.builder()
                .lectureId(id)
                .build();

        EntityModel<LectureDeleteRes> model = EntityModel.of(lectureDeleteRes);
        model.add(linkTo(methodOn(LectureController.class).deleteLecture(account, id)).withSelfRel());

        return ResponseEntity.ok().body(model);
    }

    @PostMapping("/list/search/filter")
    public ResponseEntity<?> searchListByFilter(@CurrentUser Account account,
                                                @RequestBody FilterSearchCondition condition,
                                                Pageable pageable,
                                                PagedResourcesAssembler<LectureInfo> assembler) {
        Page<LectureInfo> lectureInfoPage = lectureService.filterSearchList(account, condition, pageable);

        PagedModel<EntityModel<LectureInfo>> model = assembler.toModel(lectureInfoPage);
        return ResponseEntity.ok().body(model);
    }

    @GetMapping("/list/search/keyword")
    public ResponseEntity<?> searchListByKeyword(@CurrentUser Account account,
                                                 @NotEmpty @RequestParam String keyword,
                                                 Pageable pageable,
                                                 PagedResourcesAssembler<LectureInfo> assembler) {
        Page<LectureInfo> lectureInfoPage = lectureEsService.getListContainKeyword(account, keyword, pageable);

        PagedModel<EntityModel<LectureInfo>> model = assembler.toModel(lectureInfoPage);
        return ResponseEntity.ok().body(model);
    }

    @GetMapping("/new/list")
    public ResponseEntity<?> getNewLectures(@CurrentUser Account account,
                                            Pageable pageable,
                                            PagedResourcesAssembler<NewLectureInfo> assembler) {
        Page<NewLectureInfo> lecturePage = lectureService.getNewLecturesInfo(account, pageable);

        PagedModel<EntityModel<NewLectureInfo>> model = assembler.toModel(lecturePage);
        return ResponseEntity.ok().body(model);
    }

    @GetMapping("/popular/list")
    public ResponseEntity<?> getPopularLectures(@CurrentUser Account account,
                                                Pageable pageable,
                                                PagedResourcesAssembler<LectureInfo> assembler) {
        Page<LectureInfo> lectureInfoPage = lectureService.getPopularLecturesInfo(account, pageable);

        PagedModel<EntityModel<LectureInfo>> model = assembler.toModel(lectureInfoPage);
        return ResponseEntity.ok().body(model);
    }

    @GetMapping("/manage/list")
    public ResponseEntity<?> manageList(@CurrentUser Account account,
                                        Pageable pageable,
                                        PagedResourcesAssembler<MyLectureInfo> assembler) {
        Page<MyLectureInfo> lectureInfoPage = lectureService.findMyLectureInfoList(account, pageable);

        PagedModel<EntityModel<MyLectureInfo>> model = assembler.toModel(lectureInfoPage);
        return ResponseEntity.ok().body(model);
    }

    @PostMapping("/upload")
    public String upload(Authentication authentication, @RequestParam("file") MultipartFile file) throws IOException {
        return s3Uploader.upload(file, "lecture", authentication.getName());
    }

    @GetMapping(value = "/instructor/info/creator")
    public ResponseEntity<?> findInstructorInfoForLecture(@NotNull @RequestParam Long lectureId) {
        LectureCreatorInfo lectureCreatorInfo = lectureService.findLectureCreatorInfo(lectureId);

        EntityModel<LectureCreatorInfo> model = EntityModel.of(lectureCreatorInfo);
        model.add(linkTo(methodOn(LectureController.class).findInstructorInfoForLecture(lectureId)).withSelfRel());
        model.add(Link.of("/docs/api.html#resource-lecture-find-instructor-info").withRel("profile"));
        return ResponseEntity.ok().body(model);
    }

    @GetMapping
    public ResponseEntity<?> findLecture(@CurrentUser Account account,
                                         @NotNull @RequestParam Long id) {
        LectureDetail lectureDetail = lectureService.findLectureDetailInfo(id, account);

        EntityModel<LectureDetail> model = EntityModel.of(lectureDetail);
        model.add(linkTo(methodOn(LectureController.class).findLecture(account, id)).withSelfRel());
        model.add(Link.of("/docs/api.html#resource-lecture-find-info").withRel("profile"));
        return ResponseEntity.ok().body(model);
    }

    @GetMapping("/{id}/like")
    public ResponseEntity<?> readLectureMark(@CurrentUser Account account,
                                         @PathVariable("id") Long lectureId) {
        boolean isMarked = lectureMarkService.existLectureMark(account, lectureId);

        LectureMarkModel model = new LectureMarkModel(isMarked);
        model.add(linkTo(methodOn(LectureController.class).readLectureMark(account, lectureId)).withSelfRel());

        return ResponseEntity.ok().body(model);
    }

    @PostMapping("/like")
    public ResponseEntity<?> markLikeLecture(@CurrentUser Account account,
                                             @Valid @RequestBody MarkLectureInfo markLectureInfo,
                                             BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException();
        }

        MarkLectureResult markLectureResult = lectureService.markLecture(account, markLectureInfo.getLectureId());

        EntityModel<MarkLectureResult> model = EntityModel.of(markLectureResult);
        model.add(linkTo(methodOn(LectureController.class).markLikeLecture(account, markLectureInfo, result)).withSelfRel());
        model.add(Link.of("/docs/api.html#resource-lecture-mark-like").withRel("profile"));
        return ResponseEntity.ok().body(model);
    }

    @DeleteMapping("/unlike")
    public ResponseEntity<?> unmarkLikeLecture(@CurrentUser Account account,
                                               @Valid @RequestBody UnmarkLectureInfo unmarkLectureInfo,
                                               BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException();
        }

        lectureService.unmarkLecture(account.getId(), unmarkLectureInfo.getLectureId());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/like/list")
    public ResponseEntity<?> findLikeLectureList(@CurrentUser Account account,
                                                 Pageable pageable,
                                                 PagedResourcesAssembler<LikeLectureInfo> assembler) {
        Page<Lecture> likeLecturePage = lectureService.findLikeLectures(account, pageable);
        Page<LikeLectureInfo> likeLectureInfoPage = lectureService.mapToLikeLectureInfos(likeLecturePage);

        PagedModel<EntityModel<LikeLectureInfo>> model = assembler.toModel(likeLectureInfoPage);
        return ResponseEntity.ok().body(model);
    }

    @PatchMapping("/{id}/closed")
    public ResponseEntity<?> controlLectureClosed(@CurrentUser Account account,
                                                  @PathVariable("id") Long lectureId,
                                                  @Valid @RequestBody LectureClosedInfo info,
                                                  BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException();
        }

        lectureService.updateLectureClosed(account, lectureId, info.getIsClosed());

        return ResponseEntity.noContent().build();
    }
}