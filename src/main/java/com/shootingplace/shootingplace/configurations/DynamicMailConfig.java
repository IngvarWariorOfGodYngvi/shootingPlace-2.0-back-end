package com.shootingplace.shootingplace.configurations;

import com.shootingplace.shootingplace.email.EmailConfig;
import com.shootingplace.shootingplace.email.EmailConfigRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Optional;
import java.util.Properties;

@Configuration
public class DynamicMailConfig {

    private final EmailConfigRepository repository;

    public DynamicMailConfig(EmailConfigRepository repository) {
        this.repository = repository;
    }

    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender javaMailSender() {
        Optional<EmailConfig> optionalConfig = repository.findAll().stream().findFirst();

        if (optionalConfig.isEmpty()) {
            return null;
        }

        EmailConfig config = optionalConfig.get();

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getHost());
        mailSender.setPort(config.getPort());
        mailSender.setUsername(config.getUsername());
        mailSender.setPassword(config.getPassword());
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", String.valueOf(config.isAuth()));
        props.put("mail.smtp.starttls.enable", String.valueOf(config.isStarttls()));
        props.put("mail.smtp.ssl.trust", config.getSslTrust());

        return mailSender;
    }
}
