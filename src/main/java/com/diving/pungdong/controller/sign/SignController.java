package com.diving.pungdong.controller.sign;

import com.diving.pungdong.advice.exception.SignInInputException;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.dto.account.emailCheck.EmailInfo;
import com.diving.pungdong.dto.account.emailCheck.EmailResult;
import com.diving.pungdong.dto.account.signUp.SignUpInfo;
import com.diving.pungdong.dto.auth.AuthToken;
import com.diving.pungdong.service.AccountService;
import com.diving.pungdong.service.AuthService;
import com.diving.pungdong.service.kafka.AccountKafkaProducer;
import lombok.*;
import org.modelmapper.ModelMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/sign", produces = MediaTypes.HAL_JSON_VALUE)
public class SignController {
    private final AccountService accountService;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final AccountKafkaProducer producer;

    @PostMapping("/check/email")
    public ResponseEntity<?> checkEmailExistence(@RequestBody EmailInfo emailInfo) {
        EmailResult emailResult = accountService.checkEmailExistence(emailInfo.getEmail());

        EntityModel<EmailResult> model = EntityModel.of(emailResult);
        model.add(linkTo(methodOn(SignController.class).checkEmailExistence(emailInfo)).withSelfRel());
        model.add(Link.of("/docs/api.html#resource-account-check-email").withRel("profile"));

        return ResponseEntity.ok().body(model);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody SignInReq signInReq) {
        Account account = accountService.findAccountByEmail(signInReq.getEmail());
        accountService.checkCorrectPassword(signInReq, account);

        AuthToken authToken = authService.getAuthToken(String.valueOf(account.getId()), signInReq.getPassword());

        WebMvcLinkBuilder selfLinkBuilder = linkTo(methodOn(SignController.class).login(signInReq));
        EntityModel<AuthToken> entityModel = EntityModel.of(authToken);
        entityModel.add(selfLinkBuilder.withSelfRel());
        entityModel.add(Link.of("/docs/api.html#resource-account-login").withRel("profile"));

        return ResponseEntity.ok().body(entityModel);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignInReq {
        @Email
        @NotEmpty
        String email;

        @NotEmpty
        String password;
    }

    @Data
    @AllArgsConstructor
    static class SignInResponse {
        String accessToken;
        String refreshToken;
    }

    @PostMapping(value = "/sign-up")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpInfo signUpInfo, BindingResult result) {
        if (result.hasErrors()) {
            throw new SignInInputException();
        }

        accountService.checkDuplicationOfEmail(signUpInfo.getEmail());
        signUpInfo.setPassword(passwordEncoder.encode(signUpInfo.getPassword()));

        Account student = modelMapper.map(signUpInfo, Account.class);
        student.setRoles(Set.of(Role.STUDENT));
        accountService.saveAccount(student);
        producer.sendAccountInfo(String.valueOf(student.getId()), student.getPassword(), student.getRoles());

        WebMvcLinkBuilder selfLinkBuilder = linkTo(methodOn(SignController.class).signUp(signUpInfo, result));
        URI createUri = selfLinkBuilder.toUri();

        SignUpRes signUpRes = SignUpRes.builder()
                .email(signUpInfo.getEmail())
                .nickName(signUpInfo.getNickName())
                .build();

        EntityModel<SignUpRes> model = EntityModel.of(signUpRes);
        model.add(selfLinkBuilder.withSelfRel());
        model.add(Link.of("/docs/api.html#resource-account-create").withRel("profile"));
        model.add(linkTo(methodOn(SignController.class).login(new SignInReq(signUpInfo.getEmail(), signUpInfo.getPassword()))).withRel("signin"));

        return ResponseEntity.created(createUri).body(model);
    }

    @Data
    @Builder
    static class SignUpRes {
        String email;
        String nickName;
    }

    @PostMapping("/addInstructorRole")
    public ResponseEntity<EntityModel<AddInstructorRoleRes>> changeToInstructor(Authentication authentication,
                                                                                @RequestPart("request") AddInstructorRoleReq request,
                                                                                @RequestPart("profile") List<MultipartFile> profiles,
                                                                                @RequestPart("certificate") List<MultipartFile> certificates) throws IOException {
        Account updatedAccount = accountService.updateAccountToInstructor(authentication.getName(), request, profiles, certificates);

        AddInstructorRoleRes addInstructorRoleRes = AddInstructorRoleRes.builder()
                .email(updatedAccount.getEmail())
                .userName(updatedAccount.getNickName())
                .roles(updatedAccount.getRoles())
                .build();

        EntityModel<AddInstructorRoleRes> model = EntityModel.of(addInstructorRoleRes);
        model.add(linkTo(methodOn(SignController.class).changeToInstructor(authentication, request, profiles, certificates)).withSelfRel());
        return ResponseEntity.ok().body(model);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddInstructorRoleReq {
        @NotEmpty
        private String phoneNumber;
        @NotEmpty
        private String groupName;
        @NotEmpty
        private String description;
    }

    @Data
    @AllArgsConstructor
    @Builder
    static class AddInstructorRoleRes {
        private String email;
        private String userName;
        private Set<Role> roles;
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
