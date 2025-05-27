package com.sofrecom.backend.services;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private MimeMessage mimeMessage;

    @BeforeEach
    public void setUp() {
        mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendEmail_ValidInputs_ShouldSendEmail() throws MessagingException {  //NOSONAR
        String toEmail = "recipient@example.com";
        String subject = "Test Subject";
        String text = "Your temporary password is: 123456";

        doNothing().when(mailSender).send(any(MimeMessage.class));
        doNothing().when(mimeMessage).setFrom(anyString());
        doNothing().when(mimeMessage).setRecipients(any(), anyString());
        doNothing().when(mimeMessage).setSubject(anyString());
        doNothing().when(mimeMessage).setContent(anyString(), anyString());

        emailService.sendEmail(toEmail, subject, text);

        verify(mailSender).createMimeMessage();
        verify(mimeMessage).setFrom("noreply.sofbiblio@gmail.com");
        verify(mimeMessage).setRecipients(Message.RecipientType.TO, toEmail);
        verify(mimeMessage).setSubject("Password");
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mimeMessage).setContent(contentCaptor.capture(), eq("text/html; charset=utf-8"));
        assertEquals("<h1>Password</h1>Your temporary password is: 123456</br>", contentCaptor.getValue());
        verify(mailSender).send(mimeMessage);
    }
    @Test
    void sendEmail_NullToEmail_ShouldThrowMessagingException() throws MessagingException {  //NOSONAR
        doThrow(new MessagingException("Recipient email cannot be null"))
                .when(mimeMessage).setRecipients(any(), eq((String) null));

        assertThrows(MessagingException.class, () -> emailService.sendEmail(null, "Subject", "Text"));
        verify(mailSender).createMimeMessage();
        verify(mimeMessage).setFrom("noreply.sofbiblio@gmail.com");
        verify(mimeMessage).setRecipients(Message.RecipientType.TO, (String) null);
        verify(mimeMessage, never()).setSubject(anyString());
        verify(mimeMessage, never()).setContent(anyString(), anyString());
        verify(mailSender, never()).send(any(MimeMessage.class));
    }


    @Test
    void sendEmail_InvalidToEmail_ShouldThrowMessagingException() throws MessagingException {  //NOSONAR
        doThrow(new MessagingException("Invalid email")).when(mimeMessage).setRecipients(any(), eq("invalid-email"));

        assertThrows(MessagingException.class, () -> emailService.sendEmail("invalid-email", "Subject", "Text"));
        verify(mailSender).createMimeMessage();
        verify(mimeMessage).setRecipients(Message.RecipientType.TO, "invalid-email");
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendEmail_NullSubject_ShouldUseDefaultSubject() throws MessagingException {  //NOSONAR
        String toEmail = "recipient@example.com";
        String text = "Your temporary password is: 123456";

        doNothing().when(mailSender).send(any(MimeMessage.class));
        doNothing().when(mimeMessage).setFrom(anyString());
        doNothing().when(mimeMessage).setRecipients(any(), anyString());
        doNothing().when(mimeMessage).setSubject(anyString());
        doNothing().when(mimeMessage).setContent(anyString(), anyString());

        emailService.sendEmail(toEmail, null, text);

        verify(mimeMessage).setSubject("Password");
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_NullText_ShouldSendEmptyContent() throws MessagingException {  //NOSONAR
        String toEmail = "recipient@example.com";
        String subject = "Test Subject";

        doNothing().when(mailSender).send(any(MimeMessage.class));
        doNothing().when(mimeMessage).setFrom(anyString());
        doNothing().when(mimeMessage).setRecipients(any(), anyString());
        doNothing().when(mimeMessage).setSubject(anyString());
        doNothing().when(mimeMessage).setContent(anyString(), anyString());

        emailService.sendEmail(toEmail, subject, null);

        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mimeMessage).setContent(contentCaptor.capture(), eq("text/html; charset=utf-8"));
        assertEquals("<h1>Password</h1>null</br>", contentCaptor.getValue());
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_EmptyText_ShouldSendEmptyContent() throws MessagingException {  //NOSONAR
        String toEmail = "recipient@example.com";
        String subject = "Test Subject";

        doNothing().when(mailSender).send(any(MimeMessage.class));
        doNothing().when(mimeMessage).setFrom(anyString());
        doNothing().when(mimeMessage).setRecipients(any(), anyString());
        doNothing().when(mimeMessage).setSubject(anyString());
        doNothing().when(mimeMessage).setContent(anyString(), anyString());

        emailService.sendEmail(toEmail, subject, "");

        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mimeMessage).setContent(contentCaptor.capture(), eq("text/html; charset=utf-8"));
        assertEquals("<h1>Password</h1></br>", contentCaptor.getValue());
        verify(mailSender).send(mimeMessage);
    }



    @Test
    void sendEmail_SenderEmail_ShouldUseHardcodedEmail() throws MessagingException {  //NOSONAR
        String toEmail = "recipient@example.com";
        String subject = "Test Subject";
        String text = "Your temporary password is: 123456";

        doNothing().when(mailSender).send(any(MimeMessage.class));
        doNothing().when(mimeMessage).setFrom(anyString());
        doNothing().when(mimeMessage).setRecipients(any(), anyString());
        doNothing().when(mimeMessage).setSubject(anyString());
        doNothing().when(mimeMessage).setContent(anyString(), anyString());

        emailService.sendEmail(toEmail, subject, text);

        verify(mimeMessage).setFrom("noreply.sofbiblio@gmail.com");
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_ContentType_ShouldBeHtmlUtf8() throws MessagingException {  //NOSONAR
        String toEmail = "recipient@example.com";
        String subject = "Test Subject";
        String text = "Your temporary password is: 123456";

        doNothing().when(mailSender).send(any(MimeMessage.class));
        doNothing().when(mimeMessage).setFrom(anyString());
        doNothing().when(mimeMessage).setRecipients(any(), anyString());
        doNothing().when(mimeMessage).setSubject(anyString());
        doNothing().when(mimeMessage).setContent(anyString(), anyString());

        emailService.sendEmail(toEmail, subject, text);

        verify(mimeMessage).setContent(anyString(), eq("text/html; charset=utf-8"));
        verify(mailSender).send(mimeMessage);
    }
}