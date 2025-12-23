package com.shootingplace.shootingplace.email;

import com.shootingplace.shootingplace.contributions.ContributionEntity;
import com.shootingplace.shootingplace.enums.MailToggleOptions;
import com.shootingplace.shootingplace.enums.MailType;
import com.shootingplace.shootingplace.history.HistoryEntity;
import com.shootingplace.shootingplace.history.HistoryRepository;
import com.shootingplace.shootingplace.history.LicensePaymentHistoryEntity;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.strategies.EmailStrategy;
import com.shootingplace.shootingplace.strategies.ProfileContext;
import com.shootingplace.shootingplace.utils.CryptoUtil;
import com.shootingplace.shootingplace.utils.Mapping;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmailService {

    private JavaMailSender mailSender;
    private final SentEmailRepository sentEmailRepository;
    private final EmailConfigRepository emailConfigRepository;
    private final MemberRepository memberRepository;
    private final HistoryRepository historyRepository;
    private final EmailSendListRepository emailSendListRepository;
    private final Environment environment;
    private final ScheduledEmailRepository scheduledEmailRepository;
    private final ProfileContext profileContext;


    private final Logger LOG = LogManager.getLogger(getClass());

    public EmailService(@Autowired(required = false) JavaMailSender mailSender, SentEmailRepository sentEmailRepository, EmailConfigRepository emailConfigRepository, MemberRepository memberRepository, HistoryRepository historyRepository, EmailSendListRepository emailSendListRepository, Environment environment, ScheduledEmailRepository scheduledEmailRepository, List<ProfileContext> contexts) {
        this.mailSender = mailSender;
        this.sentEmailRepository = sentEmailRepository;
        this.emailConfigRepository = emailConfigRepository;
        this.memberRepository = memberRepository;
        this.historyRepository = historyRepository;
        this.emailSendListRepository = emailSendListRepository;
        this.environment = environment;
        this.scheduledEmailRepository = scheduledEmailRepository;
        String profile = environment.getActiveProfiles()[0];
        this.profileContext = contexts.stream()
                .filter(ctx -> ctx.getClass().getAnnotation(Profile.class).value()[0].equals(profile))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Brak profilu"));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendRichEmail(EmailRequest request, String mailType, String memberUUID) throws MessagingException {
        EmailConfig configEntity = emailConfigRepository.findAll().stream().findFirst().orElse(null);
        if (configEntity == null) return;
        mailSender = createMailSender(configEntity);
        EmailStrategy strategy = profileContext.getEmailStrategy();
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(configEntity.getUsername());
        helper.setTo(request.getTo());
        helper.setBcc(strategy.getBcc());
        helper.setSubject(request.getSubject());
        helper.setText(request.getHtmlContent(), true); // HTML
        // Załączniki
        if (request.getAttachments() != null) {
            for (EmailRequest.Attachment attachment : request.getAttachments()) {
                byte[] fileBytes = Base64.getDecoder().decode(attachment.getContentBase64());
                helper.addAttachment(attachment.getFilename(), new ByteArrayResource(fileBytes), attachment.getContentType());
            }
        }
        // Inline images
        if (request.getInlineImages() != null) {
            for (EmailRequest.InlineImage image : request.getInlineImages()) {
                byte[] imageBytes = Base64.getDecoder().decode(image.getContentBase64());
                helper.addInline(image.getContentId(), new ByteArrayResource(imageBytes), image.getContentType());
            }
        }
        sendAndSaveEmail(request, message, mailType, memberUUID);
    }
//
//    private String getBcc() {
//        String activeProfile = environment.getActiveProfiles()[0];
//        switch (activeProfile) {
//            case "test":
//            case "prod":
//                return "odautomatu@ksdziesiatka.pl";
//            case "rcs":
//                return "biuro@rcspanaszew.pl";
//        }
//
//        return activeProfile;
//    }

    private String sendAndSaveEmail(EmailRequest request, MimeMessage message, String mailType, String memberUUID) {
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElse(null);

        SentEmail log = new SentEmail();

        log.setRecipient(request.getTo());
        log.setSubject(request.getSubject());
        log.setContent(request.getHtmlContent());
        log.setSentAt(LocalDateTime.now());
        log.setMailType(mailType);
        log.setMemberUUID(memberEntity != null ? memberEntity.getUuid() : null);
        try {
            log.setSuccess(true);
            if (!checkSendingEmails()) {
                LOG.info("Udaję, że wysyłam mail");
            } else {
//                mailSender.send(message);
                LOG.info("Mail został wysłany do:{}", log.getRecipient());
            }
            SentEmail save = sentEmailRepository.save(log);
            if (memberEntity != null) {
                HistoryEntity history = historyRepository.findById(memberEntity.getHistory().getUuid()).orElseThrow(EntityNotFoundException::new);
                Set<SentEmail> sentEmailsHistory = history.getSentEmailsHistory();
                sentEmailsHistory.add(save);
                history.setSentEmailsHistory(sentEmailsHistory);
                historyRepository.save(history);
                LOG.info("Dodaję maila do historii {}", memberEntity.getFullName());
            }
            LOG.info("Wysłano wiadomość i zapisano w bazie");
            return "Wysłano wiadomość i zapisano w bazie";
        } catch (Exception e) {
            log.setSuccess(false);
            log.setErrorMessage(e.getMessage());
            LOG.error(e.getMessage());
            SentEmail save = sentEmailRepository.save(log);
            if (memberEntity != null) {
                HistoryEntity history = historyRepository.findById(memberEntity.getHistory().getUuid()).orElseThrow(EntityNotFoundException::new);
                Set<SentEmail> sentEmailsHistory = history.getSentEmailsHistory();
                sentEmailsHistory.add(save);
                history.setSentEmailsHistory(sentEmailsHistory);
                historyRepository.save(history);
                LOG.info("Dodaję maila do historii {}", memberEntity.getFullName());
            }
            LOG.info("NIE WYSŁANO WIADOMOŚCI DO: {} i zapisano w bazie", request.getTo());
            return "NIE WYSŁANO WIADOMOŚCI DO: " + request.getTo() + " i zapisano w bazie";

        }
    }

    // 1 Przypomnienie o Składkach Członkowskich
    public ResponseEntity<?> sendRemindersForActiveOneMonthBefore() {
        if (!checkSendingEmails())
            return ResponseEntity.badRequest().body("Opcja wysyłania wiadomości email jest wyłączona");
        memberRepository.findAllByErasedFalseAndActiveTrue()
                .stream()
                .filter(f -> f.getHistory().getContributionList() != null)
                .filter(f -> f.getHistory().getContributionList().getFirst().getValidThru().isAfter(LocalDate.now()) && f.getHistory().getContributionList().getFirst().getValidThru().isBefore(LocalDate.now().plusMonths(1)))
                .forEach(e -> {
                    String validThru = "BRAK SKŁADEK";
                    if (e.getHistory().getContributionList() != null) {
                        if (e.getHistory().getContributionList().getFirst() != null) {
                            validThru = e.getHistory().getContributionList().getFirst().getValidThru().format(dateFormat());
                        }
                    }
                    boolean alreadySent = !sentEmailRepository.getEmailsByMemberUUIDAndMailTypeAndSuccessTrueAndSentAtIsBetween(
                            e.getUuid(),
                            MailType.SUBSCRIPTION_REMINDER_BEFORE.getName(),
                            LocalDateTime.now().minusMonths(1),
                            LocalDateTime.now()).isEmpty();
                    boolean notYetSent = !scheduledEmailRepository.findTodayByMemberAndMailType(e.getUuid(),
                            MailType.SUBSCRIPTION_REMINDER_BEFORE.getName()).isEmpty();
                    if (alreadySent || notYetSent) {
                        LOG.info("Wiadomość do: {} nie będzie wysłana bo była wysłana mniej niż miesiąc temu.", e.getEmail());
                    } else {
                        ScheduledEmail email = new ScheduledEmail();
                        email.setRecipient(e.getEmail());
                        email.setScheduledFor(LocalDateTime.now().plusMinutes(60));
                        email.setMailType(MailType.SUBSCRIPTION_REMINDER_BEFORE.getName());
                        email.setMemberUUID(e.getUuid());
                        String activeProfile = environment.getActiveProfiles()[0];
                        String name = getClubName();
                        email.setSubject("Przypomnienie o Składkach Członkowskich " + name);
                        email.setHtmlContent("<p>Dzień Dobry,</p>\n" +
                                "<p>Przypominamy, że za nie długo twoja składka członkowska straci ważność.</p>\n" +
                                "<p>Twoja składka wygasa dnia: <b>" + validThru + "</b></p>\n" +
                                "<p>Prosimy o opłacenie składki w najbliższym czasie.</p>\n" +
                                "<p>Przypominamy również, że osoba zalegająca ze składkami członkowskimi" +
                                (activeProfile.equals("prod") || activeProfile.equals("test") ? " powyżej 6 miesięcy " : "") +
                                "może zostać usunięta z Klubu.</p>\n" +
                                "<p>Pozdrawiamy</p>\n" +
                                "<p>Zespół " + name + "</p>\n" +
                                "<div>-------------</div>\n" +
                                (activeProfile.equals("rcs") ?
                                        "<div>Konto bankowe: BNP Paribas Bank Polska SA</div>\n" +
                                                "<div>Nr: PL 83 1600 1462 1854 6971 5000 0001</div>" : "") +
                                "<div>Wiadomość automatyczna - prosimy na nią nie odpowiadać.<br />");
                        scheduledEmailRepository.save(email);
                        LOG.info("Zapisano mail do późniejszej wysyłki {}", email.getRecipient());
                    }
                });
        return ResponseEntity.ok("Wywołano przypomnienie o składce dla aktywnych");
    }

    // 2 Przypomnienie o składkach
    public ResponseEntity<?> sendRemindersForNonActive() {
        if (!checkSendingEmails())
            return ResponseEntity.badRequest().body("Opcja wysyłania wiadomości email jest wyłączona");
        memberRepository.findAllByErasedFalseAndActiveFalse().forEach(e -> {
            String validThru = "BRAK SKŁADEK";
            if (e.getHistory().getContributionList() != null) {
                if (e.getHistory().getContributionList().getFirst() != null) {
                    validThru = e.getHistory().getContributionList().getFirst().getValidThru().format(dateFormat());
                }
            }
            boolean alreadySent = !sentEmailRepository.getEmailsByMemberUUIDAndMailTypeAndSuccessTrueAndSentAtIsBetween(
                    e.getUuid(),
                    MailType.SUBSCRIPTION_REMINDER.getName(),
                    LocalDateTime.now().minusMonths(1),
                    LocalDateTime.now()).isEmpty();
            boolean notYetSent = !scheduledEmailRepository.findTodayByMemberAndMailType(e.getUuid(),
                    MailType.SUBSCRIPTION_REMINDER.getName()).isEmpty();
            if (alreadySent || notYetSent) {
                LOG.info("Wiadomość do: {} nie będzie wysłana bo była wysłana mniej niż miesiąc temu.", e.getEmail());
            } else {
                ScheduledEmail email = new ScheduledEmail();
                email.setRecipient(e.getEmail());
                email.setScheduledFor(LocalDateTime.now().plusMinutes(60));
                email.setMailType(MailType.SUBSCRIPTION_REMINDER.getName());
                email.setMemberUUID(e.getUuid());
                String activeProfile = environment.getActiveProfiles()[0];
                String name = getClubName();
                email.setSubject("Przypomnienie o Składkach Członkowskich " + name);
                email.setHtmlContent("<p>Dzień Dobry,</p>\n" +
                        "<p>Przypominamy o konieczności opłacenia składki członkowskiej.</p>\n" +
                        "<p>Twoja składka wygasła dnia: <b>" + validThru + "</b></p>\n" +
                        "<p>Prosimy o opłacenie składek najbliższym czasie.</p>\n" +
                        "<p>Przypominamy również, że osoba zalegająca ze składkami członkowskimi" +
                        (activeProfile.equals("prod") || activeProfile.equals("test") ? " powyżej 6 miesięcy " : "") +
                        "może zostać usunięta z Klubu.</p>\n" +
                        "<p>Pozdrawiamy</p>\n" +
                        "<p>Zespół " + name + "</p>\n" +
                        "<div>-------------</div>\n" +
                        (activeProfile.equals("rcs") ?
                                "<div>Konto bankowe: BNP Paribas Bank Polska SA</div>\n" +
                                        "<div>Nr: PL 83 1600 1462 1854 6971 5000 0001</div>" : "") +
                        "<div>Wiadomość automatyczna - prosimy na nią nie odpowiadać.<br />");
                scheduledEmailRepository.save(email);
                LOG.info("Zapisano mail do późniejszej wysyłki {}", email.getRecipient());
            }
        });
        return ResponseEntity.ok("Wywołano przypomnienie o składce dla nieaktywnych");
    }

    // 3
    public void sendCongratulationsOnTheAnniversary() {
        if (!checkSendingEmails()) return;
        memberRepository.findAllByErasedFalse()
                .stream()
                .filter(f -> {
                    LocalDate of = f.getJoinDate();
                    LocalDate now = LocalDate.now();
                    Integer[] x = new Integer[3];
                    x[0] = (now.getYear() - of.getYear());
                    x[1] = (now.getMonthValue() - of.getMonthValue());
                    x[2] = (now.getDayOfMonth() - of.getDayOfMonth());
                    return x[0] > 0 && x[1] == 0 && x[2] == 0;
                })
                .forEach(e -> {
                    String s = "Dokładnie rok temu";
                    if (LocalDate.now().getYear() - e.getJoinDate().getYear() > 1) {
                        s = "To już kolejny rok jak";
                    }
                    ScheduledEmail email = new ScheduledEmail();
                    email.setRecipient(e.getEmail());
                    email.setScheduledFor(LocalDateTime.now().plusMinutes(1));
                    email.setMailType(MailType.CONGRATULATIONS_ANNIVERSSARY.getName());
                    email.setMemberUUID(e.getUuid());
                    email.setSubject("Dziękujemy, że jesteś z nami!\n");
                    String name = getClubName();
                    email.setHtmlContent("<p>Dzień dobry,</p>\n" +
                            "<p>" + s + " dołączyłeś do <strong>" + name + "</strong>!</p>\n" +
                            "<p>Chcemy Ci podziękować za ten wspólny czas i zaangażowanie. Cieszymy się, że jesteś częścią naszej społeczności pasjonatów strzelectwa.</p>\n" +
                            "<p>Mamy nadzieję, że kolejny rok przyniesie Ci jeszcze więcej celnych strzałów i satysfakcji z treningów oraz udziału w zawodach.</p>\n" +
                            "<p>Do zobaczenia na strzelnicy!</p>\n" +
                            "<p>Pozdrawiamy</p>\n" +
                            "<p>Zespół " + name + "</p>\n" +
                            "<div>-------------</div>\n" +
                            "<div>Wiadomość automatyczna - prosimy na nią nie odpowiadać.<br />");
                    scheduledEmailRepository.save(email);
                    LOG.info("Zapisano mail do późniejszej wysyłki {}", email.getRecipient());
                });

    }

    // 4
    public void sendRegistrationConfirmation(String memberUUID) {
        if (!checkSendingEmails()) return;
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        ScheduledEmail email = new ScheduledEmail();
        email.setRecipient(memberEntity.getEmail());
        EmailStrategy strategy = profileContext.getEmailStrategy();
        String dates = strategy.getDatesString();
        String ahrefSite = strategy.getAHrefSite();
        String name = getClubName();
        email.setSubject("Witamy w Gronie Klubowiczów " + name);
        String s = memberEntity.getSex() ? "Zapisałeś" : "Zapisałaś";
        email.setHtmlContent("<p>Gratulacje!</p>\n" +
                "<p>" + s + " się właśnie do " + name + ".</p>\n" +
                "<p>Twój numer Klubowy to: " + memberEntity.getLegitimationNumber() + "</p>\n" +
                "<p>Nasze dni i godziny funkcjonowania Klubu to:</p>\n" +
                dates +
                "<p>Kalendarz imprez oraz aktualne informacje znajdziesz na naszej stronie:<br />" + ahrefSite + "</p>\n" +
                "<p>Pozdrawiamy</p>\n" +
                "<p>Zespół " + name + "</p>\n" +
                "<div>-------------</div>\n" +
                "<div>Wiadomość automatyczna - prosimy na nią nie odpowiadać.<br />");
        email.setScheduledFor(LocalDateTime.now().plusMinutes(1));
        email.setMailType(MailType.REGISTRATION_CONFIRMATION.getName());
        email.setMemberUUID(memberEntity.getUuid());
        scheduledEmailRepository.save(email);
        LOG.info("Zapisano mail do późniejszej wysyłki {}", email.getRecipient());


    }

    // 5
    public void sendContributionConfirmation(String memberUUID) {
        if (!checkSendingEmails()) return;
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        ContributionEntity contributionEntity = memberEntity.getHistory().getContributionList().getFirst();
        LocalDate paymentDay = contributionEntity.getPaymentDay();
        String validThru = contributionEntity.getValidThru().format(dateFormat());
        ScheduledEmail email = new ScheduledEmail();
        email.setRecipient(memberEntity.getEmail());
        String name = getClubName();
        email.setSubject("Potwierdzenie Opłacenia Składki Członkowskiej " + name);
        email.setHtmlContent("<p>Dnia <b>" + paymentDay + "</b> opłacono twoje składki członkowskie.</p>\n" +
                "<p>Data ważności Twoich aktualnych składek członkowskich to: <b>" + validThru + "</b></p><br/>\n" +
                "<p>Dziękujemy!</p>\n" +
                "<p>Zespół " + name + "</p>\n" +
                "<div>-------------</div>\n" +
                "<div>Wiadomość automatyczna - prosimy na nią nie odpowiadać.</div>");
        email.setScheduledFor(LocalDateTime.now().plusMinutes(1));
        email.setMailType(MailType.CONTRIBUTION_CONFIRMATION.getName());
        email.setMemberUUID(memberEntity.getUuid());
        scheduledEmailRepository.save(email);
        LOG.info("Zapisano mail do późniejszej wysyłki {}", email.getRecipient());

    }

    // 6
    public void sendLicensePaymentConfirmation(String memberUUID) {
        if (!checkSendingEmails()) return;
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        LicensePaymentHistoryEntity licensePaymentHistoryEntity = memberEntity.getHistory().getLicensePaymentHistory().getFirst();
        String paymentDate = LocalDate.now().format(dateFormat());
        int validForYear = licensePaymentHistoryEntity.getValidForYear() + 1;
        ScheduledEmail email = new ScheduledEmail();
        email.setRecipient(memberEntity.getEmail());
        String name = getClubName();
        email.setSubject("Potwierdzenie Opłacenia Licencji zawodniczej " + name);
        email.setHtmlContent("<p>Dnia <b>" + paymentDate + "</b> opłacono licencję zawodniczą na rok " + validForYear + ".</p>\n" +
                "<p>W najbliższym czasie zostanie dokonana opłata w systemie PZSS</p><br/>\n" +
                "<p>Dziękujemy!</p>\n" +
                "<p>Zespół " + name + "</p>\n" +
                "<div>-------------</div>\n" +
                "<div>Wiadomość automatyczna - prosimy na nią nie odpowiadać.</div>");
        email.setScheduledFor(LocalDateTime.now().plusMinutes(2));
        email.setMailType(MailType.LICENSE_PAYMENT_CONFIRMATION.getName());
        email.setMemberUUID(memberEntity.getUuid());
        scheduledEmailRepository.save(email);
        LOG.info("Zapisano mail do późniejszej wysyłki {}", email.getRecipient());

    }

    // 7
    public ResponseEntity<?> sendSingleEmail(EmailRequest request) {
        if (!checkSendingEmails())
            return ResponseEntity.badRequest().body("Opcja wysyłania wiadomości email jest wyłączona");

        Optional<MemberEntity> byEmail = memberRepository.findAllByErasedFalse().stream().filter(f -> f.getEmail().equals(request.getTo())).findFirst();

        if (byEmail.isPresent()) {
            try {
                ScheduledEmail email = new ScheduledEmail();
                email.setRecipient(byEmail.get().getEmail());
                email.setScheduledFor(LocalDateTime.now().plusMinutes(3));
                email.setMailType(MailType.CUSTOM.getName());
                email.setMemberUUID(byEmail.get().getUuid());
                email.setSubject(request.getSubject());
                email.setHtmlContent(request.getHtmlContent());
                scheduledEmailRepository.save(email);
                LOG.info("Zapisano mail do późniejszej wysyłki {}", email.getRecipient());
                sendRichEmail(request, MailType.CUSTOM.getName(), byEmail.get().getUuid());
                return ResponseEntity.ok("Zapisano mail do późniejszej wysyłki " + email.getRecipient());
            } catch (MessagingException ex) {
                ex.printStackTrace();
                return ResponseEntity.badRequest().body("Wiadomość do " + request.getTo() + " nie zostanie wysłana - Brak Maila w bazie");
            }
        }
        return null;
    }

    // 8
    public ResponseEntity<?> sendTestEmail(EmailRequest request) throws MessagingException {
        if (!checkSendingEmails())
            return ResponseEntity.badRequest().body("Opcja wysyłania wiadomości email jest wyłączona");
        EmailConfig configEntity = emailConfigRepository.findAll().stream().findFirst().orElse(null);
        if (configEntity == null) return ResponseEntity.badRequest().body("brak konfiguracji połączenia z mailem");
        mailSender = createMailSender(configEntity);
        EmailStrategy strategy = profileContext.getEmailStrategy();
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(configEntity.getUsername());
        helper.setTo(request.getTo());
        helper.setBcc(strategy.getBcc());
        helper.setSubject(request.getSubject());
        helper.setText(request.getHtmlContent(), true);
        // Załączniki
        if (request.getAttachments() != null) {
            for (EmailRequest.Attachment attachment : request.getAttachments()) {
                byte[] fileBytes = Base64.getDecoder().decode(attachment.getContentBase64());
                helper.addAttachment(attachment.getFilename(), new ByteArrayResource(fileBytes), attachment.getContentType());
            }
        }
        // Inline images
        if (request.getInlineImages() != null) {
            for (EmailRequest.InlineImage image : request.getInlineImages()) {
                byte[] imageBytes = Base64.getDecoder().decode(image.getContentBase64());
                helper.addInline(image.getContentId(), new ByteArrayResource(imageBytes), image.getContentType());
            }
        }
        String sent = sendAndSaveEmail(request, message, MailType.TEST.getName(), "");
        return ResponseEntity.ok(sent);
    }

    // 9
    public ResponseEntity<?> sendCustomEmails(EmailRequest request, List<String> emailsList) {
        if (!checkSendingEmails())
            return ResponseEntity.badRequest().body("Opcja wysyłania wiadomości email jest wyłączona");
        List<String> responses = new ArrayList<>();
        emailsList.forEach(e -> {
            Optional<MemberEntity> byEmail = memberRepository.findAllByErasedFalse().stream().filter(f -> f.getEmail().equals(e)).findFirst();
            if (byEmail.isPresent()) {
                try {
                    ScheduledEmail email = new ScheduledEmail();
                    request.setTo(e);
                    email.setRecipient(byEmail.get().getEmail());
                    email.setScheduledFor(LocalDateTime.now().plusMinutes(60));
                    email.setMailType(MailType.CUSTOM.getName());
                    email.setMemberUUID(byEmail.get().getUuid());
                    email.setSubject(request.getSubject());
                    email.setHtmlContent(request.getHtmlContent());
                    scheduledEmailRepository.save(email);
                    LOG.info("Zapisano mail do późniejszej wysyłki {}", email.getRecipient());
                    sendRichEmail(request, MailType.CUSTOM.getName(), byEmail.get().getUuid());
                    responses.add("Zapisano mail do późniejszej wysyłki " + email.getRecipient());
                } catch (MessagingException ex) {
                    responses.add("Wiadomość do " + e + " nie zostanie wysłana - Brak Maila w bazie");
                    ex.printStackTrace();
                }
            }
        });
        return ResponseEntity.ok(responses);
    }

    private boolean checkSendingEmails() {
        return Boolean.parseBoolean(environment.getProperty("sendMail"));
    }

    public ResponseEntity<?> saveConnection(EmailConfig emailConfig) {
        LOG.info("Zapisuję połączenie: {}\n{}\n{}", emailConfig.getConnectionName(), emailConfig.getHost(), emailConfig.getUsername());
        emailConfigRepository.save(emailConfig);
        return ResponseEntity.ok("zapisano połączenie");
    }

    public ResponseEntity<?> getAllConnections() {
        List<EmailConfig> list = new ArrayList<>();
        emailConfigRepository.findAll().forEach(e -> {
            e.setPassword("###");
            list.add(e);
        });
        return ResponseEntity.ok(list);
    }

    public ResponseEntity<?> deleteScheduledMail(String uuid) {
        scheduledEmailRepository.delete(scheduledEmailRepository.getByUuid(uuid));
        return ResponseEntity.ok("skasowano");
    }

    public ResponseEntity<?> saveScheduledEmail(EmailRequest request, String uuid) {
        scheduledEmailRepository.getByUuid(uuid).setHtmlContent(request.getHtmlContent());
        return ResponseEntity.ok("Zmieniono treść wiadomości");
    }

    private JavaMailSender createMailSender(EmailConfig config) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getHost());
        mailSender.setPort(config.getPort());
        mailSender.setUsername(config.getUsername());
        mailSender.setPassword(config.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", config.isAuth());
        props.put("mail.smtp.starttls.enable", config.isStarttls());
        props.put("mail.smtp.ssl.trust", config.getSslTrust());

        return mailSender;
    }

    public ResponseEntity<?> editConnection(EmailConfig newConfig, String uuid) {

        EmailConfig oldConfig = emailConfigRepository.findById(uuid).orElseThrow(EntityNotFoundException::new);

        if (newConfig.getConnectionName() != null && !newConfig.getConnectionName().isEmpty()) {
            oldConfig.setConnectionName(newConfig.getConnectionName());
        }
        if (newConfig.getHost() != null && !newConfig.getHost().isEmpty()) {
            oldConfig.setHost(newConfig.getHost());
        }
        if (newConfig.getPort() != null) {
            oldConfig.setPort(newConfig.getPort());
        }
        if (newConfig.getUsername() != null && !newConfig.getUsername().isEmpty()) {
            oldConfig.setUsername(newConfig.getUsername());
        }
        if (newConfig.getPassword() != null && !newConfig.getPassword().isEmpty()) {
            oldConfig.setPassword(CryptoUtil.encrypt(newConfig.getPassword()));
        }

        emailConfigRepository.save(oldConfig);

        return ResponseEntity.ok("Zapisano");
    }

    public ResponseEntity<?> getScheduledEmails() {
        List<ScheduledEmailDTO> scheduledEmailDTOList = new ArrayList<>();
        scheduledEmailRepository.findAll().forEach(e -> {
            ScheduledEmailDTO map = Mapping.map(e);
            map.setMemberName(memberRepository.findById(e.getMemberUUID()).orElseThrow(EntityNotFoundException::new).getFullName());
            scheduledEmailDTOList.add(map);
        });
        scheduledEmailDTOList.sort(Comparator.comparing(ScheduledEmailDTO::getScheduledFor).reversed());
        return ResponseEntity.ok(scheduledEmailDTOList);

    }

    public ResponseEntity<?> getSentEmails(LocalDate firstDate, LocalDate secondDate) {
        List<SentEmailDTO> sentEmailDTOList = new ArrayList<>();
        sentEmailRepository.getEmailSentBetween(firstDate, secondDate.plusDays(1)).forEach(e -> {
            SentEmailDTO map = Mapping.map(e);
            map.setMemberName(memberRepository.findById(e.getMemberUUID()).orElseThrow(EntityNotFoundException::new).getFullName());
            sentEmailDTOList.add(map);
        });
        sentEmailDTOList.sort(Comparator.comparing(SentEmailDTO::getSentAt).reversed());
        return ResponseEntity.ok(sentEmailDTOList);
    }

    private DateTimeFormatter dateFormat() {
        String europeanDatePattern = "dd.MM.yyyy";
        return DateTimeFormatter.ofPattern(europeanDatePattern);
    }

    private String getClubName() {
        String activeProfile = environment.getActiveProfiles()[0];
        return switch (activeProfile) {
            case "prod", "test" -> "Klubu Strzeleckiego Dziesiątka LOK w Łodzi";
            case "rcs" -> "Klubu Strzeleckiego RCS Panaszew";
            case "uks" -> "Klubu Strzeleckiego Mechanik w Tomaszowie Mazowieckim ";
            default -> "";
        };
    }

    public Map<String, Boolean> getMailingConfigList() {
        return emailSendListRepository.findAll().stream().findFirst()
                .map(list -> Arrays.stream(MailToggleOptions.values())
                        .collect(Collectors.toMap(MailToggleOptions::getName, v -> v.extract(list))))
                .orElse(Collections.emptyMap());
    }

    public String setMailingConfigList(Map<String, Boolean> map) {
        EmailSendList list = emailSendListRepository.findAll().stream().findFirst().orElse(null);
        if (list == null) return "";
        list.setSendRemindersForActiveOneMonthBefore(getBool(map, MailToggleOptions.SEND_REMINDERS_FOR_ACTIVE_ONE_MONTH_BEFORE));
        list.setSendRemindersForNonActive(getBool(map, MailToggleOptions.SEND_REMINDERS_FOR_NON_ACTIVE));
        list.setSendRegistrationConfirmation(getBool(map, MailToggleOptions.SEND_REGISTRATION_CONFIRMATION));
        list.setSendCongratulationsOnTheAnniversary(getBool(map, MailToggleOptions.SEND_CONGRATULATIONS_ON_THE_ANNIVERSARY));
        list.setSendContributionConfirmation(getBool(map, MailToggleOptions.SEND_CONTRIBUTION_CONFIRMATION));
        list.setSendLicensePaymentConfirmation(getBool(map, MailToggleOptions.SEND_LICENSE_PAYMENT_CONFIRMATION));
        list.setSendIndividual(getBool(map, MailToggleOptions.SEND_INDIVIDUAL));
        emailSendListRepository.save(list);
        return map.toString();
    }

    private Boolean getBool(Map<String, Boolean> input, MailToggleOptions option) {
        return input.getOrDefault(option.getName(), false);
    }

}
