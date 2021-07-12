package com.diving.pungdong.controller.account;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.config.security.CurrentUser;
import com.diving.pungdong.controller.lectureImage.LectureImageController;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.InstructorCertificate;
import com.diving.pungdong.dto.account.delete.PasswordInfo;
import com.diving.pungdong.dto.account.instructor.certificate.InstructorCertificateInfo;
import com.diving.pungdong.dto.account.read.InstructorBasicInfo;
import com.diving.pungdong.dto.account.update.AccountUpdateInfo;
import com.diving.pungdong.dto.account.update.NickNameInfo;
import com.diving.pungdong.dto.account.update.PasswordUpdateInfo;
import com.diving.pungdong.model.SuccessResult;
import com.diving.pungdong.service.InstructorCertificateService;
import com.diving.pungdong.service.account.AccountService;
import com.diving.pungdong.dto.account.read.AccountBasicInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final InstructorCertificateService instructorCertificateService;

    @GetMapping
    public ResponseEntity<?> readAccountInfo(@CurrentUser Account account) {
        AccountBasicInfo accountBasicInfo = accountService.mapToAccountBasicInfo(account);

        EntityModel<AccountBasicInfo> model = EntityModel.of(accountBasicInfo);
        model.add(linkTo(methodOn(AccountController.class).readAccountInfo(account)).withSelfRel());
        model.add(Link.of("/docs/api.html#resource-account-read").withRel("profile"));
        return ResponseEntity.ok().body(model);
    }

    @GetMapping("/instructor")
    public ResponseEntity<?> readInstructorInfo(@CurrentUser Account account) {
        InstructorBasicInfo instructorBasicInfo = accountService.mapToInstructorBasicInfo(account);

        EntityModel<InstructorBasicInfo> model = EntityModel.of(instructorBasicInfo);
        model.add(linkTo(methodOn(AccountController.class).readInstructorInfo(account)).withSelfRel());
        model.add(Link.of("/docs/api.html#resource-account-instructor-read").withRel("profile"));
        return ResponseEntity.ok().body(model);
    }

    @GetMapping("/instructor/certificate/list")
    public ResponseEntity<?> readInstructorCertificates(@CurrentUser Account account) {
        List<InstructorCertificate> instructorCertificateList = instructorCertificateService.findInstructorCertificates(account);
        List<InstructorCertificateInfo> instructorCertificateInfos = instructorCertificateService.mapToInstructorCertificateInfos(instructorCertificateList);

        CollectionModel<InstructorCertificateInfo> model = CollectionModel.of(instructorCertificateInfos);
        model.add(linkTo(methodOn(AccountController.class).readInstructorCertificates(account)).withSelfRel());
        model.add(Link.of("/docs/api.html#resource-account-instructor-certificate-read-list").withRel("profile"));
        return ResponseEntity.ok().body(model);
    }

    @PutMapping
    public ResponseEntity<?> updateAccountInfo(@CurrentUser Account account,
                                               @Valid @RequestBody AccountUpdateInfo updateInfo,
                                               BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException();
        }

        accountService.updateAccountInfo(account, updateInfo);

        EntityModel<SuccessResult> model = EntityModel.of(new SuccessResult(true));
        model.add(linkTo(methodOn(AccountController.class).updateAccountInfo(account, updateInfo, result)).withSelfRel());
        model.add(Link.of("/docs/api.html#resource-account-update").withRel("profile"));
        return ResponseEntity.ok().body(model);
    }

    @PatchMapping("/nickName")
    public ResponseEntity<?> updateAccountNickName(@CurrentUser Account account,
                                                   @Valid @RequestBody NickNameInfo nickNameInfo,
                                                   BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException();
        }

        accountService.updateNickName(account, nickNameInfo.getNickName());

        EntityModel<SuccessResult> model = EntityModel.of(new SuccessResult(true));
        model.add(linkTo(methodOn(AccountController.class).updateAccountNickName(account, nickNameInfo, result)).withSelfRel());
        model.add(Link.of("/docs/api.html#resource-account-update-nickName").withRel("profile"));
        return ResponseEntity.ok().body(model);
    }

    @PatchMapping("/password")
    public ResponseEntity<?> updateAccountPassword(@CurrentUser Account account,
                                                   @Valid @RequestBody PasswordUpdateInfo passwordUpdateInfo,
                                                   BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException();
        }

        accountService.updatePassword(account, passwordUpdateInfo);

        EntityModel<SuccessResult> model = EntityModel.of(new SuccessResult(true));
        model.add(linkTo(methodOn(AccountController.class).updateAccountPassword(account, passwordUpdateInfo, result)).withSelfRel());
        model.add(Link.of("/docs/api.html#resource-account-update-password").withRel("profile"));
        return ResponseEntity.ok().body(model);
    }

    @DeleteMapping
    public ResponseEntity<?> removeAccount(@CurrentUser Account account,
                                           @Valid @RequestBody PasswordInfo passwordInfo,
                                           BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException();
        }

        accountService.deleteAccount(account, passwordInfo.getPassword());

        return ResponseEntity.noContent().build();
    }
}