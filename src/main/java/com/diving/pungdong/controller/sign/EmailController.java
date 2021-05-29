package com.diving.pungdong.controller.sign;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.dto.account.emailCode.EmailAuthInfo;
import com.diving.pungdong.dto.account.emailCode.EmailSendInfo;
import com.diving.pungdong.model.SuccessResult;
import com.diving.pungdong.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/email")
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/code/send")
    public ResponseEntity<?> sendEmailCode(@Valid @RequestBody EmailSendInfo emailSendInfo,
                                           BindingResult result) throws Exception {
        if (result.hasErrors()) {
            throw new BadRequestException();
        }

        emailService.sendMessage(emailSendInfo.getEmail());

        SuccessResult successResult = new SuccessResult(true);
        EntityModel<SuccessResult> model = EntityModel.of(successResult);
        model.add(linkTo(methodOn(EmailController.class).sendEmailCode(emailSendInfo, result)).withSelfRel());
        model.add(Link.of("/docs/api.html#resource-account-email-code-send").withRel("profile"));

        return ResponseEntity.ok().body(model);
    }

    @PostMapping("/code/verify")
    public ResponseEntity<?> verifyEmailCode(@Valid @RequestBody EmailAuthInfo emailAuthInfo,
                                             BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException();
        }

        SuccessResult successResult = emailService.verifyAuthCode(emailAuthInfo.getEmail(), emailAuthInfo.getCode());

        EntityModel<SuccessResult> model = EntityModel.of(successResult);
        model.add(linkTo(methodOn(EmailController.class).verifyEmailCode(emailAuthInfo, result)).withSelfRel());
        model.add(Link.of("/docs/api.html#resource-account-email-code-verify").withRel("profile"));

        return ResponseEntity.ok().body(model);
    }

}
