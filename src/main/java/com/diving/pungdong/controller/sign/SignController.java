package com.diving.pungdong.controller.sign;

import com.diving.pungdong.advice.exception.CEmailSigninFailedException;
import com.diving.pungdong.advice.exception.SignInInputException;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.security.Principal;
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
        Account account = modelMapper.map(signUpReq, Account.class);
        accountService.saveAccount(account);

        WebMvcLinkBuilder selfLinkBuilder = linkTo(methodOn(SignController.class).signup(signUpReq, result));
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

        return ResponseEntity.ok().body(refreshRes);
    }

    @Data
    @AllArgsConstructor
    static class RefreshRes {
        String accessToken;
        String refreshToken;
    }
    /**
     * TODO: 이메일 중복 검사
     */
}
