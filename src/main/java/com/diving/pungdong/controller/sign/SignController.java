package com.diving.pungdong.controller.sign;

import com.diving.pungdong.advice.exception.CEmailSigninFailedException;
import com.diving.pungdong.advice.exception.SignInInputException;
import com.diving.pungdong.config.security.JwtTokenProvider;
import com.diving.pungdong.domain.account.*;
import com.diving.pungdong.service.AccountService;
import com.diving.pungdong.service.InstructorService;
import com.diving.pungdong.service.ResponseService;
import com.diving.pungdong.service.StudentService;
import lombok.*;
import org.modelmapper.ModelMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/sign", produces = MediaTypes.HAL_JSON_VALUE)
public class SignController {

    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ModelMapper modelMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final InstructorService instructorService;
    private final StudentService studentService;


    @PostMapping(value = "/signin")
    public ResponseEntity signin(@RequestParam String email,
                                       @RequestParam String password) {
        Account account = accountService.findAccountByEmail(email);
        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw new CEmailSigninFailedException();
        }

        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());
        String refreshToken = jwtTokenProvider.createRefreshToken(String.valueOf(account.getId()));

        SignInResponse signInResponse = new SignInResponse(accessToken, refreshToken);

        WebMvcLinkBuilder selfLinkBuilder = linkTo(methodOn(SignController.class).signin(email, password));
        EntityModel<SignInResponse> entityModel = EntityModel.of(signInResponse);
        entityModel.add(selfLinkBuilder.withSelfRel());
        entityModel.add(Link.of("/docs/index.html#resource-account-login").withRel("profile"));

        return ResponseEntity.ok().body(entityModel);
    }

    @Data
    @AllArgsConstructor
    static class SignInResponse {
        String accessToken;
        String refreshToken;
    }

    @PostMapping(value = "/signup")
    public ResponseEntity signup(@Valid @RequestBody SignUpReq signUpReq, BindingResult result) {
        if (result.hasErrors()) {
            throw new SignInInputException();
        }

        signUpReq.setPassword(passwordEncoder.encode(signUpReq.getPassword()));

        if (signUpReq.getRoles().contains(Role.INSTRUCTOR)) {
            Instructor instructor = modelMapper.map(signUpReq, Instructor.class);
            instructorService.saveInstructor(instructor);
        } else if (signUpReq.getRoles().contains(Role.STUDENT)){
            Student student = modelMapper.map(signUpReq, Student.class);
            studentService.saveStudent(student);
        }

        WebMvcLinkBuilder selfLinkBuilder = linkTo(methodOn(SignController.class).signup(signUpReq, result));
        URI createUri = selfLinkBuilder.toUri();

        SignUpRes signUpRes = SignUpRes.builder()
                .email(signUpReq.getEmail())
                .userName(signUpReq.getUserName())
                .build();

        EntityModel<SignUpRes> model = EntityModel.of(signUpRes);
        model.add(selfLinkBuilder.withSelfRel());
        model.add(Link.of("/docs/index.html#resource-account-create").withRel("profile"));
        model.add(linkTo(methodOn(SignController.class).signin(signUpReq.getEmail(), signUpReq.getPassword())).withRel("signin"));

        return ResponseEntity.created(createUri).body(model);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    static class SignUpReq {
        @NotNull String email;
        @NotNull String password;
        @NotNull String userName;
        @NotNull Integer age;
        @NotNull Gender gender;
        @NotNull Set<Role> roles;
    }

    @Data
    @Builder
    static class SignUpRes {
        String email;
        String userName;
    }

    @GetMapping("/refresh")
    public ResponseEntity refresh(HttpServletRequest request) {
        String refreshToken = jwtTokenProvider.resolveToken(request);
        Long id = Long.valueOf(jwtTokenProvider.getUserPk(refreshToken));

        Account account = accountService.findAccountById(id);

        String newAccessToken = jwtTokenProvider.createAccessToken(String.valueOf(account.getId()), account.getRoles());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(String.valueOf(account.getId()));
        RefreshRes refreshRes = new RefreshRes(newAccessToken, newRefreshToken);

        EntityModel<RefreshRes> entity = EntityModel.of(refreshRes);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(methodOn(SignController.class).refresh(request));
        entity.add(selfLinkBuilder.withSelfRel());
        entity.add(Link.of("/docs/index.html#resource-account-tokenRefresh").withRel("profile"));

        return ResponseEntity.ok().body(entity);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class RefreshRes {
        String accessToken;
        String refreshToken;
    }

    @GetMapping("/logout")
    public ResponseEntity logout(@RequestBody LogoutReq logoutReq) {
        redisTemplate.opsForValue().set(logoutReq.getAccessToken(), "false", 60*60*1000, TimeUnit.MILLISECONDS);
        redisTemplate.opsForValue().set(logoutReq.getRefreshToken(), "false", 60*60*1000*14, TimeUnit.MILLISECONDS);

        EntityModel<LogoutRes> entity = EntityModel.of(new LogoutRes());
        entity.add(linkTo(methodOn(SignController.class).logout(logoutReq)).withSelfRel());
        entity.add(Link.of("/docs/index.html#resource-account-logout").withRel("profile"));

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
    /**
     * TODO: 이메일 중복 검사
     */
}
