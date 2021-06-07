package com.diving.pungdong.config.security;

import com.diving.pungdong.service.account.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final JwtTokenProvider jwtTokenProvider;
    private final AccountService accountService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String token = jwtTokenProvider.resolveToken((HttpServletRequest) servletRequest);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

//    @Override
//    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//        String token = jwtTokenProvider.resolveToken((HttpServletRequest) servletRequest);
//        Boolean isRefreshToken = jwtTokenProvider.isRefreshToken((HttpServletRequest) servletRequest);
//
//        if (isRefreshToken != null) {
//            if (!isRefreshToken && token != null && !jwtTokenProvider.validateToken(token)) {
//                handlerExceptionResolver
//                        .resolveException((HttpServletRequest)servletRequest, (HttpServletResponse)servletResponse, null, new ExpiredAccessTokenException());
//            }
//
//            if (isRefreshToken && token != null && !jwtTokenProvider.validateToken(token)) {
//                handlerExceptionResolver
//                        .resolveException((HttpServletRequest)servletRequest, (HttpServletResponse)servletResponse, null, new ExpiredRefreshTokenException());
//            }
//
//            if (isRefreshToken && token != null && jwtTokenProvider.validateToken(token)) {
//                filterChain.doFilter(servletRequest, servletResponse);
//            }
//            if (!isRefreshToken && token != null && jwtTokenProvider.validateToken(token)) {
//                if (accountService.checkValidToken(token) != null) {
//                    handlerExceptionResolver
//                            .resolveException((HttpServletRequest)servletRequest, (HttpServletResponse)servletResponse, null, new ForbiddenTokenException());
//                } else {
//                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
//                    SecurityContextHolder.getContext().setAuthentication(authentication);
//                    filterChain.doFilter(servletRequest, servletResponse);
//                }
//            }
//        } else {
//            filterChain.doFilter(servletRequest, servletResponse);
//        }
//    }
}
