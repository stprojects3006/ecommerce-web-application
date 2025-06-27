package com.dharshi.notificationservice.services;

import com.dharshi.notificationservice.dtos.ApiResponseDto;
import com.dharshi.notificationservice.dtos.MailRequestDto;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailNotificationService implements NotificationService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromMail;

    @Override
    public ResponseEntity<ApiResponseDto<?>> sendEmail(MailRequestDto requestDto) {
        try {
            String toAddress = requestDto.getTo();
            String fromAddress = fromMail;
            String senderName = "Purely";
            String subject = requestDto.getSubject();
            String content = requestDto.getBody();

            log.info("calling -- javaMailSender.createMimeMessage()");
            MimeMessage message = javaMailSender.createMimeMessage();
            log.info("message is - " + message);

            log.info("calling -- MimeMessageHelper(message);");
            MimeMessageHelper helper = new MimeMessageHelper(message);

            log.info("loading the mime details;");
            helper.setFrom(fromAddress, senderName);
            helper.setTo(toAddress);
            helper.setSubject(subject);

            helper.setText(content, true);
            log.info("loaded the mime details;");

            log.info("sending email");
            javaMailSender.send(message);
            log.info("sent email and preparing the response");
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponseDto.builder()
                            .isSuccess(true)
                            .response("Successfully sent email!")
                            .build());
        }catch (Exception e) {
            log.error("Failed to send email: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.builder()
                            .isSuccess(false)
                            .response("Unable to send email!")
                            .build());
        }
    }

}
