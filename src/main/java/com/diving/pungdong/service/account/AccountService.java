package com.diving.pungdong.service.account;

import com.diving.pungdong.advice.exception.*;
import com.diving.pungdong.config.security.UserAccount;
import com.diving.pungdong.domain.account.Account;
import com.diving.pungdong.domain.account.InstructorCertificate;
import com.diving.pungdong.domain.account.ProfilePhoto;
import com.diving.pungdong.domain.account.Role;
import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.Organization;
import com.diving.pungdong.dto.account.emailCheck.EmailResult;
import com.diving.pungdong.dto.account.instructor.InstructorConfirmResult;
import com.diving.pungdong.dto.account.instructor.InstructorInfo;
import com.diving.pungdong.dto.account.instructor.InstructorRequestInfo;
import com.diving.pungdong.dto.account.nickNameCheck.NickNameResult;
import com.diving.pungdong.dto.account.read.InstructorBasicInfo;
import com.diving.pungdong.dto.account.restore.AccountRestoreInfo;
import com.diving.pungdong.dto.account.signIn.SignInInfo;
import com.diving.pungdong.dto.account.signUp.SignUpInfo;
import com.diving.pungdong.dto.account.signUp.SignUpResult;
import com.diving.pungdong.dto.account.update.AccountUpdateInfo;
import com.diving.pungdong.dto.account.update.NickNameInfo;
import com.diving.pungdong.dto.account.update.PasswordUpdateInfo;
import com.diving.pungdong.model.SuccessResult;
import com.diving.pungdong.repo.AccountJpaRepo;
import com.diving.pungdong.service.EmailService;
import com.diving.pungdong.dto.account.read.AccountBasicInfo;
import com.diving.pungdong.service.LectureService;
import com.diving.pungdong.service.elasticSearch.LectureEsService;
import com.diving.pungdong.service.kafka.AccountKafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService {
    private final AccountJpaRepo accountJpaRepo;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AccountKafkaProducer producer;
    private final ProfilePhotoService profilePhotoService;
    private final LectureService lectureService;
    private final LectureEsService lectureEsService;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        Account account = accountJpaRepo.findById(Long.valueOf(id)).orElseThrow(CUserNotFoundException::new);

        return new UserAccount(account);
    }

    public Account saveAccount(Account account) {
        return accountJpaRepo.save(account);
    }

    @Transactional(readOnly = true)
    public Account findAccountByEmail(String email) {
        Account account = accountJpaRepo.findByEmail(email).orElseThrow(CEmailSigninFailedException::new);
        if (account.getIsDeleted()) {
            throw new NoPermissionsException("계정이 삭제되었습니다.");
        }

        return account;
    }

    public Account findAccountById(Long id) {
        return accountJpaRepo.findById(id).orElseThrow(CUserNotFoundException::new);
    }

    public void checkDuplicationOfEmail(String email) {
        Optional<Account> account = accountJpaRepo.findByEmail(email);
        if (account.isPresent()) {
            throw new EmailDuplicationException();
        }
    }

    public void checkCorrectPassword(String password, Account account) {
        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw new BadRequestException();
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

        ProfilePhoto profilePhoto = profilePhotoService.saveDefaultProfilePhoto();

        Account student = Account.builder()
                .email(signUpInfo.getEmail())
                .password(passwordEncoder.encode(signUpInfo.getPassword()))
                .gender(signUpInfo.getGender())
                .birth(signUpInfo.getBirth())
                .nickName(signUpInfo.getNickName())
                .phoneNumber(signUpInfo.getPhoneNumber())
                .roles(Set.of(Role.STUDENT))
                .profilePhoto(profilePhoto)
                .build();
        Account savedStudent = accountJpaRepo.save(student);

        producer.sendAccountInfo(savedStudent);

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

    @Transactional
    public SuccessResult saveInstructorInfo(Account account, InstructorInfo instructorInfo) {
        account.setOrganization(instructorInfo.getOrganization());
        account.setSelfIntroduction(instructorInfo.getSelfIntroduction());
        accountJpaRepo.save(account);

        return SuccessResult.builder()
                .success(true)
                .build();
    }

    @Transactional
    public void updateIsRequestCertificated(Account account) {
        account.setIsRequestCertified(true);
        accountJpaRepo.save(account);
    }

    public Page<InstructorRequestInfo> getRequestInstructor(Pageable pageable) {
        Page<Account> accountPage = accountJpaRepo.findAllRequestInstructor(pageable);

        List<InstructorRequestInfo> instructorRequestInfos = new ArrayList<>();
        for (Account account : accountPage.getContent()) {
            List<String> certificateImageUrls = mapToCertificateImageUrls(account);

            InstructorRequestInfo requestInfo = InstructorRequestInfo.builder()
                    .accountId(account.getId())
                    .email(account.getEmail())
                    .nickName(account.getNickName())
                    .phoneNumber(account.getPhoneNumber())
                    .organization(account.getOrganization())
                    .selfIntroduction(account.getSelfIntroduction())
                    .certificateImageUrls(certificateImageUrls)
                    .build();

            instructorRequestInfos.add(requestInfo);
        }

        return new PageImpl<>(instructorRequestInfos, pageable, accountPage.getTotalElements());
    }

    public List<String> mapToCertificateImageUrls(Account account) {
        List<InstructorCertificate> instructorCertificates = account.getInstructorCertificates();
        List<String> certificateImageUrls = new ArrayList<>();
        for (InstructorCertificate instructorCertificate : instructorCertificates) {
            certificateImageUrls.add(instructorCertificate.getFileURL());
        }

        return certificateImageUrls;
    }

    @Transactional
    public Account addInstructorRole(Long accountId) {
        Account account = findAccountById(accountId);

        account.getRoles().add(Role.INSTRUCTOR);
        accountJpaRepo.save(account);

        producer.sendAccountInfo(account);

        return account;
    }

    public InstructorConfirmResult mapToInstructorConfirmResult(Account account) {
        return InstructorConfirmResult.builder()
                .email(account.getEmail())
                .nickName(account.getNickName())
                .build();
    }

    public AccountBasicInfo mapToAccountBasicInfo(Account account) {
        return AccountBasicInfo.builder()
                .id(account.getId())
                .email(account.getEmail())
                .nickName(account.getNickName())
                .birth(account.getBirth())
                .phoneNumber(account.getPhoneNumber())
                .gender(account.getGender())
                .build();
    }

    public InstructorBasicInfo mapToInstructorBasicInfo(Account account) {
        return InstructorBasicInfo.builder()
                .id(account.getId())
                .organization(account.getOrganization())
                .selfIntroduction(account.getSelfIntroduction())
                .build();
    }

    @Transactional
    public void updateAccountInfo(Account account, AccountUpdateInfo updateInfo) {
        account.setBirth(updateInfo.getBirth());
        account.setGender(updateInfo.getGender());
        account.setPhoneNumber(updateInfo.getPhoneNumber());

        Account updatedAccount = accountJpaRepo.save(account);

        producer.sendAccountUpdateInfo(updatedAccount);
    }

    @Transactional
    public void updateNickName(Account account, String nickName) {
        checkDuplicationOfNickName(nickName);

        lectureEsService.updateInstructorNickName(account, nickName);

        account.setNickName(nickName);
        Account updatedAccount = accountJpaRepo.save(account);

        producer.sendAccountUpdateInfo(updatedAccount);
    }

    @Transactional
    public void updatePassword(Account account, PasswordUpdateInfo passwordUpdateInfo) {
        checkCorrectPassword(passwordUpdateInfo.getCurrentPassword(), account);

        account.setPassword(passwordEncoder.encode(passwordUpdateInfo.getNewPassword()));
        Account updatedAccount = accountJpaRepo.save(account);

        producer.sendAccountUpdateInfo(updatedAccount);
    }

    @Transactional
    public void deleteAccount(Account account, String password) {
        checkCorrectPassword(password, account);

        account.setIsDeleted(true);
        Account updatedAccount = accountJpaRepo.save(account);

        lectureService.closeAllLecture(updatedAccount);
    }

    @Transactional
    public Account updateAccountDeleted(AccountRestoreInfo accountRestoreInfo) {
        emailService.verifyAuthCode(accountRestoreInfo.getEmail(), accountRestoreInfo.getEmailAuthCode());

        Account account = accountJpaRepo.findByEmail(accountRestoreInfo.getEmail()).orElseThrow(ResourceNotFoundException::new);
        account.setIsDeleted(false);

        return account;
    }
}