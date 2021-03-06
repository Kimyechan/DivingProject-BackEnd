package com.diving.pungdong.advice;

import com.diving.pungdong.advice.exception.*;
import com.diving.pungdong.model.CommonResult;
import com.diving.pungdong.service.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@RestControllerAdvice
public class ExceptionAdvice {

    private final ResponseService responseService;

    private final MessageSource messageSource;

    @ExceptionHandler(ForbiddenTokenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    protected CommonResult invalidToken(ForbiddenTokenException e) {
        return responseService.getFailResult(Integer.parseInt(getMessage("forbiddenToken.code")), getMessage("forbiddenToken.msg"));
    }

    @ExceptionHandler(ExpiredAccessTokenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    protected CommonResult expiredAccessToken(ExpiredAccessTokenException e) {
        return responseService.getFailResult(Integer.parseInt(getMessage("expiredAccessToken.code")), getMessage("expiredAccessToken.msg"));
    }

    @ExceptionHandler(ExpiredRefreshTokenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    protected CommonResult expiredRefreshToken(ExpiredRefreshTokenException e) {
        return responseService.getFailResult(Integer.parseInt(getMessage("expiredRefreshToken.code")), getMessage("expiredRefreshToken.msg"));
    }

    @ExceptionHandler(SignInInputException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected CommonResult signInInputException(SignInInputException e){
        return responseService.getFailResult(Integer.parseInt(getMessage("signInInputException.code")), getMessage("signInInputException.msg"));
    }

    @ExceptionHandler(CUserNotFoundException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected CommonResult userNotFound(HttpServletRequest request, CUserNotFoundException e) {
        return responseService.getFailResult(Integer.parseInt(getMessage("userNotFound.code")), getMessage("userNotFound.msg"));
    }

    @ExceptionHandler(CEmailSigninFailedException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected CommonResult emailSigninFailed(HttpServletRequest request, CEmailSigninFailedException e) {
        return responseService.getFailResult(Integer.parseInt(getMessage("emailSigninFailed.code")), getMessage("emailSigninFailed.msg"));
    }

    @ExceptionHandler(CAuthenticationEntryPointException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public CommonResult authenticationEntryPointException(HttpServletRequest request, CAuthenticationEntryPointException e) {
        return responseService.getFailResult(Integer.parseInt(getMessage("entryPointException.code")), getMessage("entryPointException.msg"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public CommonResult accessDeniedException(HttpServletRequest request, AccessDeniedException e) {
        return responseService.getFailResult(Integer.parseInt(getMessage("accessDenied.code")), getMessage("accessDenied.msg"));
    }

    @ExceptionHandler(NoPermissionsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public CommonResult noPermissions(HttpServletRequest request, NoPermissionsException e) {
        return responseService.getFailResult(Integer.parseInt(getMessage("noPermissions.code")), getMessage("noPermissions.msg"));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResult resourceNotFound(ResourceNotFoundException e) {
        return responseService.getFailResult(Integer.parseInt(getMessage("resourceNotFound.code")), getMessage("resourceNotFound.msg"));
    }

    @ExceptionHandler(ReservationFullException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResult reservationFull(ReservationFullException e) {
        return responseService.getFailResult(Integer.parseInt(getMessage("reservationFull.code")), getMessage("reservationFull.msg"));
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResult badRequest(BadRequestException e) {
        return responseService.getFailResult(Integer.parseInt(getMessage("badRequest.code")), getMessage("badRequest.msg"));
    }

//    @ExceptionHandler(Exception.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    protected CommonResult defaultException(HttpServletRequest request, Exception e) {
//        // 예외 처리의 메시지를 MessageSource에서 가져오도록 수정
//        return responseService.getFailResult(Integer.valueOf(getMessage("unKnown.code")), getMessage("unKnown.msg"));
//    }

    // code정보에 해당하는 메시지를 조회합니다.
    private String getMessage(String code) {
        return getMessage(code, null);
    }
    // code정보, 추가 argument로 현재 locale에 맞는 메시지를 조회합니다.
    private String getMessage(String code, Object[] args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
