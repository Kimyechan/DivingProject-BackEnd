package com.diving.pungdong.controller.sign;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.advice.exception.SignInInputException;
import com.diving.pungdong.config.security.CurrentUser;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.InstructorCertificate;
import com.diving.pungdong.dto.account.emailCheck.EmailInfo;
import com.diving.pungdong.dto.account.emailCheck.EmailResult;
import com.diving.pungdong.dto.account.instructor.InstructorInfo;
import com.diving.pungdong.dto.account.nickNameCheck.NickNameResult;
import com.diving.pungdong.dto.account.signIn.SignInInfo;
import com.diving.pungdong.dto.account.signUp.SignUpInfo;
import com.diving.pungdong.dto.account.signUp.SignUpResult;
import com.diving.pungdong.dto.auth.AuthToken;
import com.diving.pungdong.model.SuccessResult;
import com.diving.pungdong.service.AccountService;
import com.diving.pungdong.service.AuthService;
import com.diving.pungdong.service.InstructorCertificateService;
import lombok.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/sign", produces = MediaTypes.HAL_JSON_VALUE)
public class SignController {
    private final AccountService accountService;
    private final AuthService authService;
    private final RedisTemplate<String, String> redisTemplate;
    private final InstructorCertificateService instructorCertificateService;

    @PostMapping("/check/email")
    public ResponseEntity<?> checkEmailExistence(@RequestBody EmailInfo emailInfo) {
        EmailResult emailResult = accountService.checkEmailExistence(emailInfo.getEmail());

        EntityModel<EmailResult> model = EntityModel.of(emailResult);
        model.add(linkTo(methodOn(SignController.class).checkEmailExistence(emailInfo)).withSelfRel());
        model.add(Link.of("/docs/api.html#resource-account-check-email").withRel("profile"));

        return ResponseEntity.ok().body(model);
    }

    @GetMapping("/check/nickName")
    public ResponseEntity<?> checkDuplicationNickName(@NotEmpty @RequestParam String nickName) {
        NickNameResult nickNameResult = accountService.checkDuplicationOfNickName(nickName);

        EntityModel<NickNameResult> model = EntityModel.of(nickNameResult);
        model.add(linkTo(methodOn(SignController.class).checkDuplicationNickName(nickName)).withSelfRel());
        model.add(Link.of("/docs/api.html#resource-account-check-duplication-nickName").withRel("profile"));
        return ResponseEntity.ok().body(model);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody SignInInfo signInInfo,
                                   BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException();
        }

        Account account = accountService.findAccountByEmail(signInInfo.getEmail());
        accountService.checkCorrectPassword(signInInfo, account);

        AuthToken authToken = authService.getAuthToken(String.valueOf(account.getId()), signInInfo.getPassword());

        WebMvcLinkBuilder selfLinkBuilder = linkTo(methodOn(SignController.class).login(signInInfo, result));
        EntityModel<AuthToken> entityModel = EntityModel.of(authToken);
        entityModel.add(selfLinkBuilder.withSelfRel());
        entityModel.add(Link.of("/docs/api.html#resource-account-login").withRel("profile"));

        return ResponseEntity.ok().body(entityModel);
    }

    @PostMapping(value = "/sign-up")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpInfo signUpInfo,
                                    BindingResult result) {
        if (result.hasErrors()) {
            throw new SignInInputException();
        }

        SignUpResult signUpResult = accountService.saveAccountInfo(signUpInfo);

        EntityModel<SignUpResult> model = EntityModel.of(signUpResult);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(methodOn(SignController.class).signUp(signUpInfo, result));
        model.add(selfLinkBuilder.withSelfRel());
        model.add(Link.of("/docs/api.html#resource-account-create").withRel("profile"));
        model.add(linkTo(methodOn(SignController.class).login(new SignInInfo(signUpInfo.getEmail(), signUpInfo.getPassword()), result)).withRel("login"));

        return ResponseEntity.created(selfLinkBuilder.toUri()).body(model);
    }

    @PostMapping(value = "/instructor/info")
    public ResponseEntity<?> addInstructorInfo(@CurrentUser Account account,
                                               @Valid @RequestBody InstructorInfo instructorInfo,
                                               BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException();
        }

        SuccessResult successResult = accountService.saveInstructorInfo(account, instructorInfo);

        EntityModel<SuccessResult> model = EntityModel.of(successResult);
        model.add(linkTo(methodOn(SignController.class).addInstructorInfo(account, instructorInfo, result)).withSelfRel());
        model.add(Link.of("/docs/api.html#resource-account-add-instructorInfo").withRel("profile"));

        return ResponseEntity.ok().body(model);
    }


    @PostMapping(value = "/instructor/certificate")
    public ResponseEntity<?> addInstructorCertificate(@CurrentUser Account account,
                                                      @Valid @RequestParam("certificateImages") List<MultipartFile> certificateImages) throws IOException {
        SuccessResult successResult = instructorCertificateService.saveInstructorCertificate(account, certificateImages);

        EntityModel<SuccessResult> model = EntityModel.of(successResult);
        model.add(linkTo(methodOn(SignController.class).addInstructorCertificate(account, certificateImages)).withSelfRel());
        model.add(Link.of("/docs/api.html#resource-account-add-instructor-certificate").withRel("profile"));
        return ResponseEntity.ok().body(model);
    }


    @PostMapping("/logout")
    public ResponseEntity logout(@RequestBody LogoutReq logoutReq) {
        redisTemplate.opsForValue().set(logoutReq.getAccessToken(), "false", 60 * 60 * 1000, TimeUnit.MILLISECONDS);
        redisTemplate.opsForValue().set(logoutReq.getRefreshToken(), "false", 60 * 60 * 1000 * 14, TimeUnit.MILLISECONDS);

        EntityModel<LogoutRes> entity = EntityModel.of(new LogoutRes());
        entity.add(linkTo(methodOn(SignController.class).logout(logoutReq)).withSelfRel());
        entity.add(Link.of("/docs/api.html#resource-account-logout").withRel("profile"));

        return ResponseEntity.ok().body(entity);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class LogoutReq {
        String accessToken;
        String refreshToken;
    }

    @Data
    static class LogoutRes {
        String message = "로그아웃이 완료됐습니다";
    }
}
