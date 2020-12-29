package com.diving.pungdong.config.security;

import com.diving.pungdong.advice.exception.ExpiredAccessTokenException;
import com.diving.pungdong.advice.exception.ExpiredRefreshTokenException;
import com.diving.pungdong.advice.exception.ForbiddenTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String token = jwtTokenProvider.resolveToken((HttpServletRequest) servletRequest);
        Boolean isRefreshToken = jwtTokenProvider.isRefreshToken((HttpServletRequest) servletRequest);

        if (isRefreshToken != null) {
            if (!isRefreshToken && token != null && !jwtTokenProvider.validateToken(token)) {
                throw new ExpiredAccessTokenException();
            }

            if (isRefreshToken && token != null && !jwtTokenProvider.validateToken(token)) {
                throw new ExpiredRefreshTokenException();
            }

            if (!isRefreshToken && token != null && jwtTokenProvider.validateToken(token)) {
                if (redisTemplate.opsForValue().get(token) != null) {
                    throw new ForbiddenTokenException();
                }
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
