package com.diving.pungdong.config.security;

import com.diving.pungdong.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@RequiredArgsConstructor
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final JwtTokenProvider jwtTokenProvider;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(accountService)
                .passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                    .authorizeRequests()
                        .antMatchers("/sign/sign-up", "/sign/login", "/sign/check/**", "/sign/refresh",
                                "/email/code/**").permitAll()
                        .antMatchers("/lecture/detail", "/lecture/list", "/lecture/new/list", "/lecture/popular/list", "/lecture/list/search/**",
                                "/schedule").permitAll()
                        .antMatchers(HttpMethod.GET, "/exception/**").permitAll()
                        .antMatchers("/sign/instructor/request/list", "/sign/instructor/confirm").hasRole("ADMIN")
                        .antMatchers("/lecture/create", "/lecture/update", "/lecture/delete", "/lecture/manage/list"
                                , "/location/create", "/lectureImage/create/list", "/equipment/create/list").authenticated()
                        .anyRequest().authenticated()
                .and()
                    .exceptionHandling().accessDeniedHandler(new CustomAccessDeniedHandler())
                .and()
                    .exceptionHandling().authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                .and()
                    .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, accountService, handlerExceptionResolver)
                            , UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
        web.ignoring().antMatchers("/docs/**", "/webjars/**");
    }
}
