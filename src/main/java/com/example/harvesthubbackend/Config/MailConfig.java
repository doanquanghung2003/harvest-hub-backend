package com.example.harvesthubbackend.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String host;

    @Value("${spring.mail.port:587}")
    private int port;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${spring.mail.password:}")
    private String password;

    @Value("${spring.mail.from:no-reply@harvest-hub.local}")
    private String from;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.debug", "false");

        // Nếu không có username/password, vẫn tạo bean nhưng sẽ không gửi được email
        // EmailService sẽ kiểm tra và log ra console trong trường hợp này
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            System.err.println("========================================");
            System.err.println("⚠️  WARNING: Mail credentials not configured!");
            System.err.println("Email sending will be DISABLED.");
            System.err.println("");
            System.err.println("Current configuration:");
            System.err.println("  Host: " + host);
            System.err.println("  Port: " + port);
            System.err.println("  Username: " + (username == null || username.trim().isEmpty() ? "NOT SET" : username));
            System.err.println("  Password: " + (password == null || password.trim().isEmpty() ? "NOT SET" : "***"));
            System.err.println("");
            System.err.println("To enable email sending, set environment variables:");
            System.err.println("  - MAIL_USERNAME=your-email@gmail.com");
            System.err.println("  - MAIL_PASSWORD=your-app-password");
            System.err.println("");
            System.err.println("For Gmail, you need to use an App Password (not regular password):");
            System.err.println("1. Go to https://myaccount.google.com/");
            System.err.println("2. Security → 2-Step Verification (enable if not already)");
            System.err.println("3. App passwords → Generate new app password");
            System.err.println("4. Use the generated 16-character password as MAIL_PASSWORD");
            System.err.println("========================================");
        } else {
            System.out.println("========================================");
            System.out.println("✓ Email configuration loaded successfully");
            System.out.println("  Host: " + host);
            System.out.println("  Port: " + port);
            System.out.println("  Username: " + username);
            System.out.println("  Password: " + (password != null && !password.isEmpty() ? "*** (length: " + password.length() + ")" : "NOT SET"));
            // Debug: Log password length and first/last chars (for debugging only)
            if (password != null && !password.isEmpty()) {
                System.out.println("  Password length: " + password.length());
                System.out.println("  Password contains spaces: " + password.contains(" "));
                if (password.length() > 0) {
                    System.out.println("  Password first char: '" + password.charAt(0) + "'");
                    System.out.println("  Password last char: '" + password.charAt(password.length() - 1) + "'");
                }
            }
            System.out.println("========================================");
        }

        return mailSender;
    }
}

