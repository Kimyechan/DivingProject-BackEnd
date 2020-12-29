package com.diving.pungdong.controller;

import com.diving.pungdong.advice.exception.CUserNotFoundException;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.model.CommonResult;
import com.diving.pungdong.model.ListResult;
import com.diving.pungdong.model.SingleResult;
import com.diving.pungdong.repo.AccountJpaRepo;
import com.diving.pungdong.service.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1")
public class UserController {

    private final AccountJpaRepo accountJpaRepo;
    private final ResponseService responseService; // 결과를 처리할 Service

    @GetMapping(value = "/users")
    public ListResult<Account> findAllUser() {
        // 결과데이터가 여러건인경우 getListResult를 이용해서 결과를 출력한다.
        return responseService.getListResult(accountJpaRepo.findAll());
    }

    @GetMapping(value = "/user")
    public SingleResult<Account> findUserById() {
        // SecurityContext에서 인증받은 회원의 정보를 얻어온다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        // 결과데이터가 단일건인경우 getSingleResult를 이용해서 결과를 출력한다.
        return responseService.getSingleResult(accountJpaRepo.findByEmail(email).orElseThrow(CUserNotFoundException::new));
    }

    @PutMapping(value = "/user")
    public SingleResult<Account> modify(
            Long id,
            String userName) {
        Account account = Account.builder()
                .id(id)
                .userName(userName)
                .build();
        return responseService.getSingleResult(accountJpaRepo.save(account));
    }

    @DeleteMapping(value = "/user/{id}")
    public CommonResult delete(
           @PathVariable Long id) {
        accountJpaRepo.deleteById(id);
        // 성공 결과 정보만 필요한경우 getSuccessResult()를 이용하여 결과를 출력한다.
        return responseService.getSuccessResult();
    }
}
