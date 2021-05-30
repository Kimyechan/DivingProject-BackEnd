package com.diving.pungdong.service;

import com.diving.pungdong.advice.exception.BadRequestException;
import com.diving.pungdong.advice.exception.CEmailSigninFailedException;
import com.diving.pungdong.advice.exception.CUserNotFoundException;
import com.diving.pungdong.advice.exception.EmailDuplicationException;
import com.diving.pungdong.config.security.UserAccount;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.InstructorImgCategory;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.dto.account.emailCheck.EmailResult;
import com.diving.pungdong.dto.account.nickNameCheck.NickNameResult;
import com.diving.pungdong.dto.account.signIn.SignInInfo;
import com.diving.pungdong.dto.account.signUp.SignUpInfo;
import com.diving.pungdong.dto.account.signUp.SignUpResult;
import com.diving.pungdong.repo.AccountJpaRepo;
import com.diving.pungdong.service.kafka.AccountKafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.diving.pungdong.controller.sign.SignController.AddInstructorRoleReq;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService {
    private final AccountJpaRepo accountJpaRepo;
    private final RedisTemplate<String, String> redisTemplate;
    private final InstructorImageService instructorImageService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AccountKafkaProducer producer;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        Account account = accountJpaRepo.findById(Long.valueOf(id)).orElseThrow(CUserNotFoundException::new);
        return new UserAccount(account);
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

    public void checkDuplicationOfEmail(String email) {
        Optional<Account> account = accountJpaRepo.findByEmail(email);
        if (account.isPresent()) {
            throw new EmailDuplicationException();
        }
    }

    public void checkCorrectPassword(SignInInfo signInInfo, Account account) {
        if (!passwordEncoder.matches(signInInfo.getPassword(), account.getPassword())) {
            throw new CEmailSigninFailedException();
        }
    }

    public EmailResult checkEmailExistence(String email) {
        Boolean isExisted = accountJpaRepo.existsByEmail(email);

        return EmailResult.builder()
                .existed(isExisted)
                .build();
    }

    @Transactional
    public SignUpResult saveAccountInfo(SignUpInfo signUpInfo) {
        emailService.verifyAuthCode(signUpInfo.getEmail(), signUpInfo.getVerifyCode());
        checkDuplicationOfNickName(signUpInfo.getNickName());
        checkDuplicationOfEmail(signUpInfo.getEmail());

        Account student = Account.builder()
                .email(signUpInfo.getEmail())
                .password(passwordEncoder.encode(signUpInfo.getPassword()))
                .gender(signUpInfo.getGender())
                .birth(signUpInfo.getBirth())
                .nickName(signUpInfo.getNickName())
                .phoneNumber(signUpInfo.getPhoneNumber())
                .roles(Set.of(Role.STUDENT))
                .build();
        Account savedStudent = accountJpaRepo.save(student);

        producer.sendAccountInfo(String.valueOf(student.getId()), student.getPassword(), student.getRoles());

        return SignUpResult.builder()
                .email(savedStudent.getEmail())
                .nickName(savedStudent.getNickName())
                .build();
    }

    public NickNameResult checkDuplicationOfNickName(String nickName) {
        Optional<Account> account = accountJpaRepo.findByNickName(nickName);
        if (account.isPresent()) {
            throw new BadRequestException("닉네임이 중복되었습니다");
        }

        return NickNameResult.builder()
                .isExisted(false)
                .build();
    }
}
