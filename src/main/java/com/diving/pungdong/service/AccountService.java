package com.diving.pungdong.service;

import com.diving.pungdong.advice.exception.CEmailSigninFailedException;
import com.diving.pungdong.advice.exception.CUserNotFoundException;
import com.diving.pungdong.config.S3Uploader;
import com.diving.pungdong.controller.lecture.LectureController;
import static com.diving.pungdong.controller.sign.SignController.AddInstructorRoleReq;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.InstructorImage;
import com.diving.pungdong.domain.account.InstructorImgCategory;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.repo.AccountJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService {
    private final AccountJpaRepo accountJpaRepo;
    private final RedisTemplate<String, String> redisTemplate;
    private final InstructorImageService instructorImageService;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        Account account = accountJpaRepo.findById(Long.valueOf(id)).orElseThrow(CUserNotFoundException::new);
        return new User(account.getEmail(), account.getPassword(), authorities(account.getRoles()));
    }

    private Collection<? extends GrantedAuthority> authorities(Set<Role> roles) {
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                .collect(Collectors.toList());
    }

    public Account saveAccount(Account account) {
        return accountJpaRepo.save(account);
    }

    public Account findAccountByEmail(String email) {
        return accountJpaRepo.findByEmail(email).orElseThrow(CEmailSigninFailedException::new);
    }

    public Account findAccountById(Long id) {
        return accountJpaRepo.findById(id).orElseThrow(CUserNotFoundException::new);
    }

    public String checkValidToken(String token) {
        return redisTemplate.opsForValue().get(token);
    }

    public Account updateAccountToInstructor(String email,
                                             AddInstructorRoleReq request,
                                             List<MultipartFile> profiles,
                                             List<MultipartFile> certificates) throws IOException {
        Account account = accountJpaRepo.findByEmail(email).orElseThrow(CEmailSigninFailedException::new);
        account.setPhoneNumber(request.getPhoneNumber());
        account.setGroupName(request.getGroupName());
        account.setDescription(request.getDescription());
        account.getRoles().add(Role.INSTRUCTOR);

        Account updateAccount = accountJpaRepo.save(account);

        instructorImageService.uploadInstructorImages(email, profiles, updateAccount, "profile", InstructorImgCategory.PROFILE);
        instructorImageService.uploadInstructorImages(email, certificates, updateAccount, "certificate", InstructorImgCategory.CERTIFICATE);

        return updateAccount;
    }
}
