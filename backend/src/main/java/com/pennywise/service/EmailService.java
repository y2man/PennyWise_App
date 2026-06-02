package com.pennywise.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${pennywise.mail.from:noreply@pennywise.app}")
    private String from;

    @Value("${spring.mail.username:NOT_SET}")
    private String mailUsername;

    private boolean isConfigured(String value) {
        return value != null && !value.trim().isEmpty() && !"NOT_SET".equals(value) && !value.contains("your-email");
    }

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String toEmail, String otp, String userName) {
        // If mail not configured, just log to console (dev mode)
        if (!isConfigured(mailUsername)) {
            log.warn("╔══════════════════════════════════════╗");
            log.warn("║  EMAIL NOT CONFIGURED — DEV MODE     ║");
            log.warn("║  OTP for {}: {}          ║", toEmail, otp);
            log.warn("╚══════════════════════════════════════╝");
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(toEmail);
            msg.setSubject("Your PennyWise reset code: " + otp);
            msg.setText("Hi " + userName + ",\n\nYour reset code is:\n\n    " + otp
                    + "\n\nExpires in 10 minutes.\n\n- PennyWise");
            mailSender.send(msg);
            log.info("OTP sent to {}", toEmail);
        } catch (MailException e) {
            log.error("Email send failed: {}", e.getMessage());
            log.warn(">>> FALLBACK DEV OTP for {}: {} <<<", toEmail, otp);
        }
    }
}
