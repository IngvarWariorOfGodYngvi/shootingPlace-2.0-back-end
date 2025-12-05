package com.shootingplace.shootingplace.email;

import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.utils.Mapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailScheduler {

    private final EmailQueueService emailQueueService;
    private final EmailService emailService;
    private final ScheduledEmailRepository scheduledEmailRepository;
    private final MemberRepository memberRepository;

    private final Logger LOG = LogManager.getLogger(getClass());

    public EmailScheduler(EmailQueueService emailQueueService, EmailService emailService, ScheduledEmailRepository scheduledEmailRepository, MemberRepository memberRepository) {
        this.emailQueueService = emailQueueService;
        this.emailService = emailService;
        this.scheduledEmailRepository = scheduledEmailRepository;
        this.memberRepository = memberRepository;
    }
    @Scheduled(fixedDelay = 6000)
    public void processQueue() {
        if (!emailQueueService.isEmpty()) {
            EmailQueue item = emailQueueService.dequeue();
            try {
                String s = item.getMemberEntity() != null ? item.getMemberEntity().getUuid() : null;
                emailService.sendRichEmail(item.getRequest(), item.getMailType(), s);
            } catch (Exception e) {
                LOG.error(e.getStackTrace());
                LOG.error("Błąd wysyłania do: " + item.getRequest().getTo() + ", " + e.getMessage());
            }
        }
    }
    @Scheduled(fixedRate = 30000)
    public void processScheduledEmails() {
        scheduledEmailRepository.findAllByScheduledForBefore(LocalDateTime.now()).forEach(e -> {
            EmailRequest request = new EmailRequest();
            request.setTo(e.getRecipient());
            request.setSubject(e.getSubject());
            request.setHtmlContent(e.getHtmlContent());
            Mapping.map(request, e.getMailType(), memberRepository.getOne(e.getMemberUUID()));
            emailQueueService.enqueue(Mapping.map(request, e.getMailType(), memberRepository.getOne(e.getMemberUUID())));
            scheduledEmailRepository.delete(e);

        });
    }
}


