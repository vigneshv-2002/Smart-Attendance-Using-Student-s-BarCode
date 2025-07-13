package com.attendance.util;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Properties;

/**
 * Utility class for sending emails with attachments
 */
public class EmailSender {
    
    /**
     * Send an email with an attendance report attachment
     */
    public static void sendAttendanceReport(
            String smtpHost,
            int smtpPort,
            String username,
            String password,
            String from,
            String to,
            String subject,
            String body,
            File attachment) throws Exception {
        
        // Set up mail server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", smtpPort);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        
        // Create session with authenticator
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        
        // Create message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        
        // Create multipart message
        Multipart multipart = new MimeMultipart();
        
        // Create text part
        BodyPart textPart = new MimeBodyPart();
        textPart.setText(body);
        multipart.addBodyPart(textPart);
        
        // Create attachment part
        if (attachment != null && attachment.exists()) {
            BodyPart attachmentPart = new MimeBodyPart();
            DataSource source = new FileDataSource(attachment);
            attachmentPart.setDataHandler(new DataHandler(source));
            attachmentPart.setFileName(attachment.getName());
            multipart.addBodyPart(attachmentPart);
        }
        
        // Set message content
        message.setContent(multipart);
        
        // Send message
        Transport.send(message);
    }
}