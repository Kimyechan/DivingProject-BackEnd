package com.diving.pungdong.controller;

import com.diving.pungdong.config.security.JwtTokenProvider;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.repo.AccountJpaRepo;
import com.diving.pungdong.advice.exception.CEmailSigninFailedException;
import com.diving.pungdong.model.CommonResult;
import com.diving.pungdong.model.SingleResult;
import com.diving.pungdong.service.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/sign")
public class SignController {

    private final AccountJpaRepo accountJpaRepo;
    private final JwtTokenProvider jwtTokenProvider;
    private final ResponseService responseService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping(value = "/signin")
    public SingleResult<String> signin(@RequestParam String email,
                                       @RequestParam String password) {
        Account account = accountJpaRepo.findByEmail(email).orElseThrow(CEmailSigninFailedException::new);
        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw new CEmailSigninFailedException();
        }

        return responseService.getSingleResult(jwtTokenProvider.createToken(String.valueOf(account.getId()), account.getRoles()));
    }

    @PostMapping(value = "/signup")
    public CommonResult signin(@RequestBody Account account) {
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        accountJpaRepo.save(account);

        return responseService.getSuccessResult();
    }
    /**
     * TODO: 이메일 중복 검사
     */
}
