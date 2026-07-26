package backend.reports.util;

import backend.dto.NotificationSettingsDTO;
import backend.service.SettingsService;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Properties;

@Component
public class EmailUtil {

    private static final Logger logger = LoggerFactory.getLogger(EmailUtil.class);

    @Autowired
    private SettingsService settingsService;

    public void sendReportEmail(String recipient, String subject, String body, String filePath) {
        NotificationSettingsDTO settings = settingsService.getNotificationSettings();

        if (settings == null || Boolean.FALSE.equals(settings.getEmailEnabled())) {
            logger.warn("Email dispatch skipped: Automated email notifications are disabled in Settings.");
            throw new RuntimeException("Email notifications are disabled in Control Center & Settings.");
        }

        try {
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(settings.getSmtpHost() != null ? settings.getSmtpHost().trim() : "smtp.gmail.com");
            mailSender.setPort(settings.getSmtpPort() != null ? settings.getSmtpPort() : 587);
            mailSender.setUsername(settings.getSenderEmail() != null ? settings.getSenderEmail().trim() : "");
            mailSender.setPassword(settings.getSenderPassword() != null ? settings.getSenderPassword().trim() : "");

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(settings.getSenderEmail());
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(body);

            if (filePath != null) {
                File attachment = new File(filePath);
                if (attachment.exists()) {
                    helper.addAttachment(attachment.getName(), attachment);
                }
            }

            mailSender.send(message);
            logger.info("Real SMTP email successfully dispatched to: {} via {}", recipient, settings.getSmtpHost());

        } catch (Exception e) {
            logger.error("Failed to send real SMTP email to {}: {}", recipient, e.getMessage(), e);
            throw new RuntimeException("Failed to send email via SMTP: " + e.getMessage());
        }
    }
}

