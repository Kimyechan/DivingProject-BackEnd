package com.diving.pungdong.controller.sign;

import com.diving.pungdong.advice.exception.CEmailSigninFailedException;
import com.diving.pungdong.config.security.JwtTokenProvider;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.Gender;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.model.SingleResult;
import com.diving.pungdong.repo.AccountJpaRepo;
import com.diving.pungdong.service.AccountService;
import com.diving.pungdong.service.ResponseService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
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

        WebMvcLinkBuilder selfLinkBuilder = linkTo(methodOn(SignController.class).signin(email, ""));
        EntityModel<SignInResponse> entityModel = EntityModel.of(signInResponse);
        entityModel.add(selfLinkBuilder.withSelfRel());

        return ResponseEntity.ok().body(entityModel);
    }

    @Data
    @AllArgsConstructor
    static class SignInResponse {
        String accessToken;
    }

    @PostMapping(value = "/signup")
    public ResponseEntity signin(@RequestBody AccountDto accountDto) {
        accountDto.setPassword(passwordEncoder.encode(accountDto.getPassword()));
        Account account = modelMapper.map(accountDto, Account.class);
        accountService.saveAccount(account);

        WebMvcLinkBuilder selfLinkBuilder = linkTo(methodOn(SignController.class).signin(accountDto));
        URI createUri = selfLinkBuilder.toUri();

        return ResponseEntity.created(createUri).build();
    }

    @Data
    @Builder
    static class AccountDto {
        String email;
        @JsonIgnore
        String password;
        String userName;
        Integer age;
        Gender gender;
        Set<Role> roles;
    }

    /**
     * TODO: 이메일 중복 검사
     */
}
