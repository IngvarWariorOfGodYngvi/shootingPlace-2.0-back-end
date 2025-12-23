package com.shootingplace.shootingplace.contributions;


import com.google.common.hash.Hashing;
import com.shootingplace.shootingplace.email.EmailService;
import com.shootingplace.shootingplace.history.HistoryEntityType;
import com.shootingplace.shootingplace.history.HistoryService;
import com.shootingplace.shootingplace.history.RecordHistory;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.strategies.ContributionStrategy;
import com.shootingplace.shootingplace.strategies.ProfileContext;
import com.shootingplace.shootingplace.users.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
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
        this.profileContext = contexts.stream().filter(ctx -> ctx.getClass().getAnnotation(Profile.class).value()[0].equals(profile)).findFirst().orElseThrow(() -> new IllegalStateException("Brak profilu"));
    }

    @RecordHistory(action = "Cntribution.add", entity = HistoryEntityType.CONTRIBUTION)
    public ResponseEntity<?> addContribution(String memberUUID, LocalDate contributionPaymentDay, Integer contributionCount) {

        MemberEntity member = memberRepository.findById(memberUUID).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono Klubowicza"));

        ContributionStrategy strategy = profileContext.getContributionStrategy();
        int count = strategy.getContributionCount(contributionCount);

        for (int i = 0; i < count; i++) {

            List<ContributionEntity> existing = member.getHistory().getContributionList();

            ContributionEntity contribution = ContributionEntity.builder().paymentDay(contributionPaymentDay).validThru(strategy.calculateValidThru(contributionPaymentDay, existing)).build();

            contributionRepository.save(contribution);
            historyService.addContribution(memberUUID, contribution);

            member.setActive(contribution.getValidThru().isAfter(LocalDate.now()));
            memberRepository.save(member);

            LOG.info("Dodano składkę dla {}", member.getFullName());
        }

        emailService.sendContributionConfirmation(memberUUID);
        return ResponseEntity.ok("Dodano składkę");
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
        return ContributionEntity.builder().paymentDay(contributionPaymentDay).validThru(validThru).acceptedBy(userRepository.findByPinCode(pin).orElseThrow(EntityNotFoundException::new).getFullName()).build();
    }

    @RecordHistory(action = "Contribution.remove", entity = HistoryEntityType.CONTRIBUTION, entityArgIndex = 1)
    public ResponseEntity<?> removeContribution(String memberUUID, String contributionUUID, String pinCode) {

        MemberEntity member = memberRepository.findById(memberUUID).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono Klubowicza"));

        ContributionEntity contribution = member.getHistory().getContributionList().stream().filter(c -> c.getUuid().equals(contributionUUID)).findFirst().orElseThrow(EntityNotFoundException::new);

        historyService.removeContribution(memberUUID, contribution);

        member.setActive(member.getHistory().getContributionList().getFirst().getValidThru().isAfter(LocalDate.now()));

        memberRepository.save(member);

        LOG.info("Usunięto składkę: {} {}", member.getSecondName(), member.getFirstName());
        return ResponseEntity.ok("Usunięto składkę");
    }


    @RecordHistory(action = "Contribution.update", entity = HistoryEntityType.CONTRIBUTION, entityArgIndex = 1)
    public ResponseEntity<?> updateContribution(String memberUUID, String contributionUUID, LocalDate paymentDay, LocalDate validThru) {

        MemberEntity member = memberRepository.findById(memberUUID).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono Klubowicza"));

        ContributionEntity contribution = contributionRepository.findById(contributionUUID).orElseThrow(EntityNotFoundException::new);

        if (paymentDay != null) {
            contribution.setPaymentDay(paymentDay);
        }
        if (validThru != null) {
            contribution.setValidThru(validThru);
        }

        contribution.setEdited(true);
        contributionRepository.save(contribution);

        member.setActive(member.getHistory().getContributionList().getFirst().getValidThru().isAfter(LocalDate.now()));

        memberRepository.save(member);

        LOG.info("Zaktualizowano składkę: {}", member.getFullName());
        return ResponseEntity.ok("Zaktualizowano składkę");
    }


}
