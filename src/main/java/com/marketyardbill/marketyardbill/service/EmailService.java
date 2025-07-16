package com.marketyardbill.marketyardbill.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

@Service
public class EmailService {

    @Value("${spring.mail.username}")
    private String from;

    @Value("${spring.mail.password}")
    private String password;

    // Default fallback recipient if 'to' is null or empty
    private static final String DEFAULT_RECEIVER = "akshaykuadale433@gmail.com";

    public void sendInvoiceEmail(String to, File pdfFile, Long invoiceId) throws MessagingException, IOException {
        // Validate or fallback to default
        if (to == null || to.trim().isEmpty()) {
            to = DEFAULT_RECEIVER;
        } else {
            to = to + "," + DEFAULT_RECEIVER;
        }

        // Email properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Create session
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        // Compose the message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject("Jarad Farm House - Invoice #" + invoiceId);

        // Email body
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText("""
            Dear Client,

            Thank you for choosing to do business with us. We truly appreciate your support and trust in our services.

            Please find attached the invoice for your reference.

            Looking forward to working together again.

            Regards,
            Harshvardhan Jarad
            Jarad Farm House
            +91 9766762525
        """);

        // PDF attachment
        MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.attachFile(pdfFile);

        // Combine body and attachment
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(attachmentPart);

        // Set the full content
        message.setContent(multipart);

        // Send the message
        Transport.send(message);
    }
}
