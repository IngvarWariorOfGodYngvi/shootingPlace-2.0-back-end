package com.shootingplace.shootingplace.contributions;


import com.google.common.hash.Hashing;
import com.shootingplace.shootingplace.strategies.ContributionStrategy;
import com.shootingplace.shootingplace.strategies.ProfileContext;
import com.shootingplace.shootingplace.email.EmailService;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.HistoryService;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.users.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Service
public class ContributionService {

    private final ContributionRepository contributionRepository;
    private final MemberRepository memberRepository;
    private final HistoryService historyService;
    private final UserRepository userRepository;

    private final EmailService emailService;

    private final Logger LOG = LogManager.getLogger(getClass());
    private final ProfileContext profileContext;


    public ContributionService(ContributionRepository contributionRepository, MemberRepository memberRepository, HistoryService historyService, Environment environment, UserRepository userRepository, EmailService emailService, List<ProfileContext> contexts) {
        this.contributionRepository = contributionRepository;
        this.memberRepository = memberRepository;
        this.historyService = historyService;
        this.userRepository = userRepository;
        this.emailService = emailService;
        String profile = environment.getActiveProfiles()[0];
        this.profileContext = contexts.stream()
                .filter(ctx -> ctx.getClass().getAnnotation(Profile.class).value()[0].equals(profile))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Brak profilu"));
    }

    public ResponseEntity<?> addContribution(String memberUUID, LocalDate contributionPaymentDay, String pinCode, Integer contributionCount) throws NoUserPermissionException {
        if (!memberRepository.existsById(memberUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }
        ContributionStrategy strategy = profileContext.getContributionStrategy();
        String pin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        ResponseEntity<?> response = null;
        contributionCount = strategy.getContributionCount(contributionCount);
        for (int i = 0; i < contributionCount; i++) {
            MemberEntity memberEntity = memberRepository.getOne(memberUUID);
            List<ContributionEntity> contributionEntityList = memberEntity.getHistory().getContributionList();
            ContributionEntity contributionEntity = ContributionEntity.builder()
                    .paymentDay(null)
                    .validThru(null)
                    .acceptedBy(userRepository.findByPinCode(pin).orElseThrow(EntityNotFoundException::new).getFullName())
                    .build();
            contributionEntity.setPaymentDay(contributionPaymentDay);
            contributionEntity.setValidThru(strategy.calculateValidThru(contributionPaymentDay,contributionEntityList));
            contributionRepository.save(contributionEntity);
            response = historyService.getStringResponseEntity(pinCode, contributionEntity, HttpStatus.OK, "Dodaj Składkę", "Przedłużono składkę " + memberEntity.getFullName());
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                historyService.addContribution(memberUUID, contributionEntity);
                LOG.info("zmieniono " + memberEntity.getSecondName());
                memberEntity.setActive(contributionEntity.getValidThru().isAfter(LocalDate.now()));
                memberRepository.save(memberEntity);
            }
        }
        emailService.sendContributionConfirmation(memberUUID);
        return response;
    }
    public ContributionEntity addFirstContribution(LocalDate contributionPaymentDay, String pinCode) {
        ContributionEntity contributionEntity = getContributionEntity(contributionPaymentDay, pinCode);
        LOG.info("utworzono pierwszą składkę");
        return contributionRepository.save(contributionEntity);
    }

    private ContributionEntity getContributionEntity(LocalDate contributionPaymentDay, String pinCode) {

        ContributionStrategy strategy = profileContext.getContributionStrategy();
        LocalDate validThru = strategy.calculateFirstValidThru(contributionPaymentDay);
        String pin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        return ContributionEntity.builder()
                .paymentDay(contributionPaymentDay)
                .validThru(validThru)
                .acceptedBy(userRepository.findByPinCode(pin).orElseThrow(EntityNotFoundException::new).getFullName())
                .build();
    }

    public ResponseEntity<?> removeContribution(String memberUUID, String contributionUUID, String pinCode) throws NoUserPermissionException {
        if (!memberRepository.existsById(memberUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);

        ContributionEntity contributionEntity = memberRepository
                .findById(memberUUID)
                .orElseThrow(EntityNotFoundException::new)
                .getHistory()
                .getContributionList()
                .stream()
                .filter(f -> f.getUuid().equals(contributionUUID)).toList().get(0);

        ResponseEntity<?> response = historyService.getStringResponseEntity(pinCode, contributionEntity, HttpStatus.OK, "Usuń ręcznie składkę", "Usunięto składkę " + memberEntity.getSecondName() + " " + memberEntity.getFirstName());
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            historyService.removeContribution(memberUUID, contributionEntity);
            LOG.info("zmieniono " + memberEntity.getSecondName());
            memberEntity.setActive(memberEntity.getHistory().getContributionList().get(0).getValidThru().isAfter(LocalDate.now()));
            memberRepository.save(memberEntity);
        }
        return response;
    }

    public ResponseEntity<?> updateContribution(String memberUUID, String contributionUUID, LocalDate paymentDay, LocalDate validThru, String pinCode) throws NoUserPermissionException {
        if (!memberRepository.existsById(memberUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }
        MemberEntity memberEntity = memberRepository.getOne(memberUUID);
        String pin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();

        ContributionEntity contributionEntity = contributionRepository.getOne(contributionUUID);

        if (paymentDay != null) {
            contributionEntity.setPaymentDay(paymentDay);
        }

        if (validThru != null) {
            contributionEntity.setValidThru(validThru);
        }
        contributionEntity.setAcceptedBy(userRepository.findByPinCode(pin).orElseThrow(EntityNotFoundException::new).getFullName());
        contributionEntity.setEdited(true);
        ResponseEntity<?> response = historyService.getStringResponseEntity(pinCode, contributionEntity, HttpStatus.OK, "Edytuj składkę", "Edytowano składkę " + memberEntity.getFullName());
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            contributionRepository.save(contributionEntity);
            memberEntity.setActive(memberEntity.getHistory().getContributionList().get(0).getValidThru().isAfter(LocalDate.now()));
            memberRepository.save(memberEntity);
        }
        return response;
    }

}
