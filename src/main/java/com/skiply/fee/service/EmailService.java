package com.skiply.fee.service;

import com.skiply.fee.dto.EmailDetails;
import com.skiply.fee.dto.Receipt;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${email.from}")
    private String fromEmail;

    @Value("${email.subject}")
    private String emailSubject;

    public void sendReceiptEmail(Receipt receipt, String toEmail) {
        try {
            EmailDetails emailDetails = prepareEmail(receipt, toEmail);
            sendHtmlMail(emailDetails);
            log.info("Receipt email sent successfully to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send receipt email: {}", e.getMessage());
        }
    }

    private EmailDetails prepareEmail(Receipt receipt, String toEmail) {
        Context context = new Context();
        String schoolEmail = "support@" + receipt.getSchoolName().replaceAll("\\s+", "").toLowerCase()+".com";

        context.setVariable("studentName", receipt.getStudentName());
        context.setVariable("studentId", receipt.getStudentId());
        context.setVariable("referenceNumber", receipt.getReferenceNumber());
        context.setVariable("transactionDateTime", receipt.getTransactionDateTime());
        context.setVariable("cardNumber", receipt.getCardNumber());
        context.setVariable("cardType", receipt.getCardType());
        context.setVariable("amount", receipt.getAmount());
        context.setVariable("schoolName", receipt.getSchoolName());
        context.setVariable("schoolEmail", schoolEmail);
        context.setVariable("grade", receipt.getGrade());

        String emailContent = templateEngine.process("receipt-email", context);

        return EmailDetails.builder()
                .recipient(toEmail)
                .subject(emailSubject)
                .message(emailContent)
                .build();
    }

    private void sendHtmlMail(EmailDetails details) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(details.getRecipient());
        helper.setSubject(details.getSubject());
        helper.setText(details.getMessage(), true);

        mailSender.send(message);
    }
}