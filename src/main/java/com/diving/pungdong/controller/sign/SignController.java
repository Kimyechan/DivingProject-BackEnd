package com.diving.pungdong.controller.sign;

import com.diving.pungdong.advice.exception.CEmailSigninFailedException;
import com.diving.pungdong.config.security.JwtTokenProvider;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.service.AccountService;
import com.diving.pungdong.service.ResponseService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Set;

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
    private final ResponseService responseService;


    @PostMapping(value = "/signin")
    public ResponseEntity signin(@RequestParam String email,
                                       @RequestParam String password) {
        Account account = accountService.findAccountByEmail(email);
        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw new CEmailSigninFailedException();
        }

        String accessToken = jwtTokenProvider.createToken(String.valueOf(account.getId()), account.getRoles());
        SignInResponse signInResponse = new SignInResponse(accessToken);

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
    }

    @PostMapping(value = "/signup")
    public ResponseEntity signup(@RequestBody SignUpReq signUpReq) {
        signUpReq.setPassword(passwordEncoder.encode(signUpReq.getPassword()));
        Account account = modelMapper.map(signUpReq, Account.class);
        accountService.saveAccount(account);

        WebMvcLinkBuilder selfLinkBuilder = linkTo(methodOn(SignController.class).signup(signUpReq));
        URI createUri = selfLinkBuilder.toUri();

        SignUpRes signUpRes = SignUpRes.builder()
                .email(account.getEmail())
                .userName(account.getUserName())
                .build();

        EntityModel<SignUpRes> model = EntityModel.of(signUpRes);
        model.add(selfLinkBuilder.withSelfRel());
        model.add(Link.of("/docs/index.html#resource-account-create").withRel("profile"));
        model.add(linkTo(methodOn(SignController.class).signin(account.getEmail(), account.getPassword())).withRel("signin"));

        return ResponseEntity.created(createUri).body(model);
    }

    @Data
    @Builder
    static class SignUpReq {
        String email;
        String password;
        String userName;
        Integer age;
        Gender gender;
        Set<Role> roles;
    }

    @Data
    @Builder
    static class SignUpRes {
        String email;
        String userName;
    }

    /**
     * TODO: 이메일 중복 검사
     */
}
