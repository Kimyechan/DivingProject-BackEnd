package com.diving.pungdong.controller.lecture;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.advice.exception.NoPermissionsException;
import com.diving.pungdong.config.security.CurrentUser;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.dto.lecture.create.LectureCreateInfo;
import com.diving.pungdong.dto.lecture.create.LectureCreateResult;
import com.diving.pungdong.dto.lecture.delete.LectureDeleteRes;
import com.diving.pungdong.dto.lecture.list.newList.NewLectureInfo;
import com.diving.pungdong.dto.lecture.list.LectureInfo;
import com.diving.pungdong.dto.lecture.list.search.FilterSearchCondition;
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
import java.io.IOException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/lecture")
public class LectureController {

    private final LectureService lectureService;
    private final LectureEsService lectureEsService;
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

//    @PostMapping("/update")
//    public ResponseEntity<EntityModel<LectureUpdateRes>> updateLecture(@CurrentUser Account account,
//                                                                       @RequestPart("request") LectureUpdateInfo lectureUpdateInfo,
//                                                                       @RequestPart("fileList") List<MultipartFile> addLectureImageFiles) throws IOException {
//        Lecture lecture = lectureService.getLectureById(lectureUpdateInfo.getId());
//
//        if (!lecture.getInstructor().getEmail().equals(account.getEmail())) {
//            throw new NoPermissionsException();
//        }
//
//        Lecture updatedLecture = lectureService.updateLectureTx(account.getEmail(), lectureUpdateInfo, addLectureImageFiles, lecture);
//        LectureUpdateRes lectureUpdateRes = LectureUpdateRes.builder()
//                .id(updatedLecture.getId())
//                .title(updatedLecture.getTitle())
//                .build();
//
//        EntityModel<LectureUpdateRes> model = EntityModel.of(lectureUpdateRes);
//        model.add(linkTo(methodOn(LectureController.class).updateLecture(account, lectureUpdateInfo, addLectureImageFiles)).withSelfRel());
//
//        return ResponseEntity.ok().body(model);
//    }

    @DeleteMapping("/delete")
    public ResponseEntity<EntityModel<LectureDeleteRes>> deleteLecture(@CurrentUser Account account, @RequestParam("id") Long id) {
        Lecture lecture = lectureService.getLectureById(id);
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

//    @GetMapping("/detail")
//    public ResponseEntity<EntityModel<LectureDetail>> getLectureDetail(@RequestParam Long id) {
//        Lecture lecture = lectureService.getLectureById(id);
//
//        LectureDetail lectureDetail = mapToLectureDetail(lecture);
//
//        EntityModel<LectureDetail> model = EntityModel.of(lectureDetail);
//        model.add(linkTo(methodOn(LectureController.class).getLectureDetail(id)).withSelfRel());
//
//        return ResponseEntity.ok().body(model);
//    }
//
//    public LectureDetail mapToLectureDetail(Lecture lecture) {
//        LectureDetail lectureDetail = LectureDetail.builder()
//                .id(lecture.getId())
//                .title(lecture.getTitle())
//                .classKind(lecture.getClassKind())
//                .organization(lecture.getOrganization())
//                .level(lecture.getLevel())
//                .description(lecture.getDescription())
//                .price(lecture.getPrice())
//                .region(lecture.getRegion())
//                .instructorId(lecture.getInstructor().getId())
//                .lectureUrlList(new ArrayList<>())
//                .equipmentList(new ArrayList<>())
//                .build();
//
//        for (LectureImage lectureImage : lecture.getLectureImages()) {
//            lectureDetail.getLectureUrlList().add(lectureImage.getFileURI());
//        }
//
//        for (Equipment equipment : lecture.getEquipmentList()) {
//            EquipmentDto equipmentDto = EquipmentDto.builder()
//                    .name(equipment.getName())
//                    .price(equipment.getPrice())
//                    .build();
//
//            lectureDetail.getEquipmentList().add(equipmentDto);
//        }
//        return lectureDetail;
//    }

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
                                        PagedResourcesAssembler<com.diving.pungdong.dto.lecture.list.mylist.LectureInfo> assembler) {
        Page<com.diving.pungdong.dto.lecture.list.mylist.LectureInfo> lectureInfoPage = lectureService.getMyLectureInfoList(account, pageable);

        PagedModel<EntityModel<com.diving.pungdong.dto.lecture.list.mylist.LectureInfo>> model = assembler.toModel(lectureInfoPage);
        return ResponseEntity.ok().body(model);
    }

    @PostMapping("/upload")
    public String upload(Authentication authentication, @RequestParam("file") MultipartFile file) throws IOException {
        return s3Uploader.upload(file, "lecture", authentication.getName());
    }
}