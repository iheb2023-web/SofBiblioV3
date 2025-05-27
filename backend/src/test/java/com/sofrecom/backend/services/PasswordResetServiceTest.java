package com.sofrecom.backend.services;

import com.sofrecom.backend.dtos.PasswordResetResponse;
import com.sofrecom.backend.entities.PasswordResetToken;
import com.sofrecom.backend.entities.User;
import com.sofrecom.backend.repositories.PasswordResetTokenRepository;
import com.sofrecom.backend.repositories.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {
    @Value("${jwt.secret-key}")
    private String password;

    @Mock
    private Environment environment;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordResetService passwordResetService;

    private User testUser;
    private PasswordResetToken testToken;
    private String testEmail;
    private String testTokenString;
    private String testPassword;

    @BeforeEach
    void setUp() throws Exception {
        testEmail = "test@example.com";
        testTokenString = "test-reset-token";
        testPassword = "newPassword123";

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail(testEmail);
        testUser.setPassword(password);

        testToken = new PasswordResetToken();
        testToken.setToken(testTokenString);
        testToken.setUser(testUser);
        testToken.setExpiryDate(Date.from(LocalDateTime.now().plusMinutes(5)
                .atZone(ZoneId.systemDefault()).toInstant()));

        // Create service instance and inject mocks manually
        passwordResetService = new PasswordResetService(environment);
        injectMockFields();
    }

    private void injectMockFields() throws Exception {
        Field tokenRepositoryField = PasswordResetService.class.getDeclaredField("tokenRepository");
        tokenRepositoryField.setAccessible(true);
        tokenRepositoryField.set(passwordResetService, tokenRepository);

        Field mailSenderField = PasswordResetService.class.getDeclaredField("mailSender");
        mailSenderField.setAccessible(true);
        mailSenderField.set(passwordResetService, mailSender);

        Field userRepositoryField = PasswordResetService.class.getDeclaredField("userRepository");
        userRepositoryField.setAccessible(true);
        userRepositoryField.set(passwordResetService, userRepository);

        Field passwordEncoderField = PasswordResetService.class.getDeclaredField("passwordEncoder");
        passwordEncoderField.setAccessible(true);
        passwordEncoderField.set(passwordResetService, passwordEncoder);
    }

    // Tests for createPasswordResetTokenForUser method
    @Test
    void createPasswordResetTokenForUser_ValidUserAndToken_ShouldCreateToken() {
        // Given
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(testToken);

        // When
        passwordResetService.createPasswordResetTokenForUser(testUser, testTokenString);

        // Then
        verify(tokenRepository).deleteByUser(testUser);
        verify(tokenRepository).save(argThat(token ->
                token.getUser().equals(testUser) &&
                        token.getToken().equals(testTokenString) &&
                        token.getExpiryDate().after(new Date())
        ));
    }

    @Test
    void createPasswordResetTokenForUser_ShouldSetExpiryDateTo5Minutes() {
        // Given
        Date beforeCall = new Date();

        // When
        passwordResetService.createPasswordResetTokenForUser(testUser, testTokenString);

        // Then
        verify(tokenRepository).save(argThat(token -> {
            Date expiryDate = token.getExpiryDate();
            long timeDifference = expiryDate.getTime() - beforeCall.getTime();
            // Should be approximately 5 minutes (300000 ms), allowing some tolerance
            return timeDifference >= 295000 && timeDifference <= 305000;
        }));
    }

    @Test
    void createPasswordResetTokenForUser_ShouldDeleteExistingTokensFirst() {
        // When
        passwordResetService.createPasswordResetTokenForUser(testUser, testTokenString);

        // Then
        verify(tokenRepository).deleteByUser(testUser);
        verify(tokenRepository).save(any(PasswordResetToken.class));
    }

    // Tests for sendPasswordResetEmail method
    @Test
    void sendPasswordResetEmail_ValidEmailAndToken_ShouldSendEmail() throws MessagingException {  //NOSONAR
        // Given
        MimeMessage mockMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMessage);

        // When
        passwordResetService.sendPasswordResetEmail(testEmail, testTokenString);

        // Then
        verify(mailSender).createMimeMessage();
        verify(mockMessage).setFrom("noreply.sofbiblio@gmail.com");
        verify(mockMessage).setRecipients(any(), eq(testEmail));
        verify(mockMessage).setSubject("Password Reset");
        verify(mockMessage).setContent(contains(testTokenString), eq("text/html; charset=utf-8"));
        verify(mailSender).send(mockMessage);
    }


    // Tests for resetPassword method
    @Test
    void resetPassword_ValidTokenAndPassword_ShouldResetPassword() {
        // Given
        when(tokenRepository.findByToken(testTokenString)).thenReturn(testToken);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(testPassword)).thenReturn("encodedNewPassword");

        // When
        PasswordResetResponse response = passwordResetService.resetPassword(testTokenString, testPassword);

        // Then
        assertNotNull(response);
        assertEquals("Password reset successfully!", response.getStatus());
        verify(passwordEncoder).encode(testPassword);
        verify(userRepository).save(argThat(user ->
                "encodedNewPassword".equals(user.getPassword())
        ));
        verify(tokenRepository).delete(testToken);
    }

    @Test
    void resetPassword_TokenNotFound_ShouldReturnTokenNotFoundMessage() {
        // Given
        when(tokenRepository.findByToken(testTokenString)).thenReturn(null);

        // When
        PasswordResetResponse response = passwordResetService.resetPassword(testTokenString, testPassword);

        // Then
        assertNotNull(response);
        assertEquals("Token not found!", response.getStatus());
        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).delete(any());
    }

    @Test
    void resetPassword_ExpiredToken_ShouldReturnTokenExpiredMessage() {
        // Given
        Date expiredDate = Date.from(LocalDateTime.now().minusMinutes(10)
                .atZone(ZoneId.systemDefault()).toInstant());
        testToken.setExpiryDate(expiredDate);
        when(tokenRepository.findByToken(testTokenString)).thenReturn(testToken);

        // When
        PasswordResetResponse response = passwordResetService.resetPassword(testTokenString, testPassword);

        // Then
        assertNotNull(response);
        assertEquals("Token expired!", response.getStatus());
        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).delete(any());
    }

    @Test
    void resetPassword_UserNotFound_ShouldReturnUserNotFoundMessage() {
        // Given
        when(tokenRepository.findByToken(testTokenString)).thenReturn(testToken);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        PasswordResetResponse response = passwordResetService.resetPassword(testTokenString, testPassword);

        // Then
        assertNotNull(response);
        assertEquals("User not found!", response.getStatus());
        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).delete(any());
    }

    @Test
    void resetPassword_ExceptionThrown_ShouldReturnNull() {
        // Given
        when(tokenRepository.findByToken(testTokenString)).thenThrow(new RuntimeException("Database error"));

        // When
        PasswordResetResponse response = passwordResetService.resetPassword(testTokenString, testPassword);

        // Then
        assertNull(response);
        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).delete(any());
    }

    @Test
    void resetPassword_NullToken_ShouldReturnTokenNotFoundMessage() {
        // When
        PasswordResetResponse response = passwordResetService.resetPassword(null, testPassword);

        // Then
        assertNotNull(response);
        assertEquals("Token not found!", response.getStatus());
    }

    @Test
    void resetPassword_EmptyToken_ShouldReturnTokenNotFoundMessage() {
        // Given
        when(tokenRepository.findByToken("")).thenReturn(null);

        // When
        PasswordResetResponse response = passwordResetService.resetPassword("", testPassword);

        // Then
        assertNotNull(response);
        assertEquals("Token not found!", response.getStatus());
    }

    // Tests for getTokenByEmail method
    @Test
    void getTokenByEmail_ValidEmail_ShouldReturnToken() {
        // Given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(tokenRepository.findByUser(testUser)).thenReturn(testToken);

        // When
        String result = passwordResetService.getTokenByEmail(testEmail);

        // Then
        assertEquals(testTokenString, result);
        verify(userRepository).findByEmail(testEmail);
        verify(tokenRepository).findByUser(testUser);
    }

    @Test
    void getTokenByEmail_UserNotFound_ShouldReturnNull() {
        // Given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // When
        String result = passwordResetService.getTokenByEmail(testEmail);

        // Then
        assertNull(result);
        verify(userRepository).findByEmail(testEmail);
        verify(tokenRepository, never()).findByUser(any());
    }

    @Test
    void getTokenByEmail_TokenNotFound_ShouldReturnNull() {
        // Given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(tokenRepository.findByUser(testUser)).thenReturn(null);

        // When
        String result = passwordResetService.getTokenByEmail(testEmail);

        // Then
        assertNull(result);
        verify(userRepository).findByEmail(testEmail);
        verify(tokenRepository).findByUser(testUser);
    }

    @Test
    void getTokenByEmail_NullEmail_ShouldReturnNull() {
        // Given
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // When
        String result = passwordResetService.getTokenByEmail(null);

        // Then
        assertNull(result);
    }

    @Test
    void getTokenByEmail_EmptyEmail_ShouldReturnNull() {
        // Given
        when(userRepository.findByEmail("")).thenReturn(Optional.empty());

        // When
        String result = passwordResetService.getTokenByEmail("");

        // Then
        assertNull(result);
    }

    // Tests for changePassword method
    @Test
    void changePassword_ValidEmailAndPassword_ShouldChangePassword() {
        // Given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(testPassword)).thenReturn("encodedNewPassword");

        // When
        PasswordResetResponse response = passwordResetService.changePassword(testEmail, testPassword);

        // Then
        assertNotNull(response);
        assertEquals("Password changed successfully!", response.getStatus());
        verify(passwordEncoder).encode(testPassword);
        verify(userRepository).save(argThat(user ->
                "encodedNewPassword".equals(user.getPassword())
        ));
    }

    @Test
    void changePassword_UserNotFound_ShouldReturnError() {
        // Given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // When
        PasswordResetResponse response = passwordResetService.changePassword(testEmail, testPassword);

        // Then
        assertNotNull(response);
        assertEquals("Error", response.getStatus());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_NullEmail_ShouldReturnError() {
        // Given
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // When
        PasswordResetResponse response = passwordResetService.changePassword(null, testPassword);

        // Then
        assertNotNull(response);
        assertEquals("Error", response.getStatus());
    }

    @Test
    void changePassword_EmptyEmail_ShouldReturnError() {
        // Given
        when(userRepository.findByEmail("")).thenReturn(Optional.empty());

        // When
        PasswordResetResponse response = passwordResetService.changePassword("", testPassword);

        // Then
        assertNotNull(response);
        assertEquals("Error", response.getStatus());
    }

    @Test
    void changePassword_NullPassword_ShouldStillProcessButEncodeNull() {
        // Given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(null)).thenReturn("encodedNullPassword");

        // When
        PasswordResetResponse response = passwordResetService.changePassword(testEmail, null);

        // Then
        assertNotNull(response);
        assertEquals("Password changed successfully!", response.getStatus());
        verify(passwordEncoder).encode(null);
        verify(userRepository).save(testUser);
    }

    @Test
    void changePassword_EmptyPassword_ShouldEncodeEmptyString() {
        // Given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("")).thenReturn("encodedEmptyPassword");

        // When
        PasswordResetResponse response = passwordResetService.changePassword(testEmail, "");

        // Then
        assertNotNull(response);
        assertEquals("Password changed successfully!", response.getStatus());
        verify(passwordEncoder).encode("");
        verify(userRepository).save(testUser);
    }
}