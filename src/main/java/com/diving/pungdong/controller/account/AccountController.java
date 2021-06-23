package com.diving.pungdong.controller.account;

import com.diving.pungdong.config.security.CurrentUser;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.dto.account.read.InstructorBasicInfo;
import com.diving.pungdong.service.account.AccountService;
import com.diving.pungdong.dto.account.read.AccountBasicInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

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
}
