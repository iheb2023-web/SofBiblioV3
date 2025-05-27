package com.sofrecom.backend.controllers;

import com.sofrecom.backend.dtos.PasswordResetResponse;
import com.sofrecom.backend.entities.User;
import com.sofrecom.backend.repositories.UserRepository;
import com.sofrecom.backend.services.PasswordResetService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetControllerTest {

    @Mock
    private PasswordResetService passwordResetService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PasswordResetController passwordResetController;

    private User user;
    private PasswordResetResponse passwordResetResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        passwordResetResponse = new PasswordResetResponse("Password reset successfully");
    }

    // ============= REQUEST PASSWORD RESET TESTS =============

    @Test
    void requestPasswordReset_Success() throws MessagingException {  //NOSONAR
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        doNothing().when(passwordResetService).createPasswordResetTokenForUser(any(User.class), anyString());
        doNothing().when(passwordResetService).sendPasswordResetEmail(eq("test@example.com"), anyString());

        ResponseEntity<String> response = passwordResetController.requestPasswordReset("test@example.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset email sent successfully", response.getBody());
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordResetService).createPasswordResetTokenForUser(any(User.class), anyString());
        verify(passwordResetService).sendPasswordResetEmail(eq("test@example.com"), anyString());
    }



    @Test
    void requestPasswordReset_NullUser() throws MessagingException {  //NOSONAR
        when(userRepository.findByEmail("test@example.com")).thenReturn(null);

        ResponseEntity<String> response = passwordResetController.requestPasswordReset("test@example.com");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User not found", response.getBody());
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordResetService, never()).createPasswordResetTokenForUser(any(User.class), anyString());
        verify(passwordResetService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void requestPasswordReset_MessagingException() throws MessagingException {  //NOSONAR
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        doNothing().when(passwordResetService).createPasswordResetTokenForUser(any(User.class), anyString());
        doThrow(new MessagingException("Email service unavailable"))
                .when(passwordResetService).sendPasswordResetEmail(eq("test@example.com"), anyString());

        assertThrows(MessagingException.class, () -> {
            passwordResetController.requestPasswordReset("test@example.com");
        });

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordResetService).createPasswordResetTokenForUser(any(User.class), anyString());
        verify(passwordResetService).sendPasswordResetEmail(eq("test@example.com"), anyString());
    }

    @Test
    void requestPasswordReset_ServiceException() throws MessagingException {  //NOSONAR
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("Service error"))
                .when(passwordResetService).createPasswordResetTokenForUser(any(User.class), anyString());

        assertThrows(RuntimeException.class, () -> {
            passwordResetController.requestPasswordReset("test@example.com");
        });

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordResetService).createPasswordResetTokenForUser(any(User.class), anyString());
        verify(passwordResetService, never()).sendPasswordResetEmail(anyString(), anyString());
    }




    @Test
    void getPasswordResetCode_Success() {  //NOSONAR
        when(passwordResetService.getTokenByEmail("test@example.com")).thenReturn("reset-token");

        String result = passwordResetController.getPasswordResetCode("test@example.com");

        assertEquals("reset-token", result);
        verify(passwordResetService).getTokenByEmail("test@example.com");
    }

    @Test
    void getPasswordResetCode_TokenNotFound() {  //NOSONAR
        when(passwordResetService.getTokenByEmail("test@example.com")).thenReturn(null);

        String result = passwordResetController.getPasswordResetCode("test@example.com");

        assertNull(result);
        verify(passwordResetService).getTokenByEmail("test@example.com");
    }

    @Test
    void getPasswordResetCode_EmptyToken() {  //NOSONAR
        when(passwordResetService.getTokenByEmail("test@example.com")).thenReturn("");

        String result = passwordResetController.getPasswordResetCode("test@example.com");

        assertEquals("", result);
        verify(passwordResetService).getTokenByEmail("test@example.com");
    }

    @Test
    void getPasswordResetCode_ServiceException() {  //NOSONAR
        when(passwordResetService.getTokenByEmail("test@example.com"))
                .thenThrow(new RuntimeException("Token service error"));

        assertThrows(RuntimeException.class, () -> {
            passwordResetController.getPasswordResetCode("test@example.com");
        });

        verify(passwordResetService).getTokenByEmail("test@example.com");
    }

    @Test
    void getPasswordResetCode_NullEmail() {
        when(passwordResetService.getTokenByEmail(isNull())).thenReturn(null);

        String result = passwordResetController.getPasswordResetCode(null);

        assertNull(result);
        verify(passwordResetService).getTokenByEmail(isNull());
    }

    // ============= RESET PASSWORD TESTS =============

    @Test
    void resetPassword_Success() {  //NOSONAR
        when(passwordResetService.resetPassword("reset-token", "newPassword")).thenReturn(passwordResetResponse);

        ResponseEntity<PasswordResetResponse> response = passwordResetController.resetPassword("reset-token", "newPassword");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset successfully", response.getBody().getStatus());
        verify(passwordResetService).resetPassword("reset-token", "newPassword");
    }

    @Test
    void resetPassword_InvalidToken() {  //NOSONAR
        PasswordResetResponse errorResponse = new PasswordResetResponse("Invalid or expired token");
        when(passwordResetService.resetPassword("invalid-token", "newPassword")).thenReturn(errorResponse);

        ResponseEntity<PasswordResetResponse> response = passwordResetController.resetPassword("invalid-token", "newPassword");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Invalid or expired token", response.getBody().getStatus());
        verify(passwordResetService).resetPassword("invalid-token", "newPassword");
    }

    @Test
    void resetPassword_ServiceException() {  //NOSONAR
        when(passwordResetService.resetPassword("reset-token", "newPassword"))
                .thenThrow(new RuntimeException("Password reset service error"));

        assertThrows(RuntimeException.class, () -> {
            passwordResetController.resetPassword("reset-token", "newPassword");
        });

        verify(passwordResetService).resetPassword("reset-token", "newPassword");
    }

    @Test
    void resetPassword_NullToken() {  //NOSONAR
        PasswordResetResponse errorResponse = new PasswordResetResponse("Invalid token");
        when(passwordResetService.resetPassword(isNull(), eq("newPassword"))).thenReturn(errorResponse);

        ResponseEntity<PasswordResetResponse> response = passwordResetController.resetPassword(null, "newPassword");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Invalid token", response.getBody().getStatus());
        verify(passwordResetService).resetPassword(isNull(), eq("newPassword"));
    }

    @Test
    void resetPassword_EmptyToken() {  //NOSONAR
        PasswordResetResponse errorResponse = new PasswordResetResponse("Invalid token");
        when(passwordResetService.resetPassword("", "newPassword")).thenReturn(errorResponse);

        ResponseEntity<PasswordResetResponse> response = passwordResetController.resetPassword("", "newPassword");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Invalid token", response.getBody().getStatus());
        verify(passwordResetService).resetPassword("", "newPassword");
    }

    @Test
    void resetPassword_NullPassword() {  //NOSONAR
        PasswordResetResponse errorResponse = new PasswordResetResponse("Invalid password");
        when(passwordResetService.resetPassword(eq("reset-token"), isNull())).thenReturn(errorResponse);

        ResponseEntity<PasswordResetResponse> response = passwordResetController.resetPassword("reset-token", null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Invalid password", response.getBody().getStatus());
        verify(passwordResetService).resetPassword(eq("reset-token"), isNull());
    }

    @Test
    void resetPassword_EmptyPassword() {  //NOSONAR
        PasswordResetResponse errorResponse = new PasswordResetResponse("Password cannot be empty");
        when(passwordResetService.resetPassword("reset-token", "")).thenReturn(errorResponse);

        ResponseEntity<PasswordResetResponse> response = passwordResetController.resetPassword("reset-token", "");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password cannot be empty", response.getBody().getStatus());
        verify(passwordResetService).resetPassword("reset-token", "");
    }

    // ============= CHANGE PASSWORD TESTS =============

    @Test
    void changePassword_Success() {  //NOSONAR
        when(passwordResetService.changePassword("test@example.com", "newPassword")).thenReturn(passwordResetResponse);

        ResponseEntity<PasswordResetResponse> response = passwordResetController.changePassword("test@example.com", "newPassword");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset successfully", response.getBody().getStatus());
        verify(passwordResetService).changePassword("test@example.com", "newPassword");
    }

    @Test
    void changePassword_UserNotFound() {  //NOSONAR
        PasswordResetResponse errorResponse = new PasswordResetResponse("User not found");
        when(passwordResetService.changePassword("nonexistent@example.com", "newPassword")).thenReturn(errorResponse);

        ResponseEntity<PasswordResetResponse> response = passwordResetController.changePassword("nonexistent@example.com", "newPassword");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User not found", response.getBody().getStatus());
        verify(passwordResetService).changePassword("nonexistent@example.com", "newPassword");
    }

    @Test
    void changePassword_ServiceException() {  //NOSONAR
        when(passwordResetService.changePassword("test@example.com", "newPassword"))
                .thenThrow(new RuntimeException("Password change service error"));

        assertThrows(RuntimeException.class, () -> {
            passwordResetController.changePassword("test@example.com", "newPassword");
        });

        verify(passwordResetService).changePassword("test@example.com", "newPassword");
    }

    @Test
    void changePassword_NullEmail() {  //NOSONAR
        PasswordResetResponse errorResponse = new PasswordResetResponse("Invalid email");
        when(passwordResetService.changePassword(isNull(), eq("newPassword"))).thenReturn(errorResponse);

        ResponseEntity<PasswordResetResponse> response = passwordResetController.changePassword(null, "newPassword");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Invalid email", response.getBody().getStatus());
        verify(passwordResetService).changePassword(isNull(), eq("newPassword"));
    }

    @Test
    void changePassword_EmptyEmail() {  //NOSONAR
        PasswordResetResponse errorResponse = new PasswordResetResponse("Email cannot be empty");
        when(passwordResetService.changePassword("", "newPassword")).thenReturn(errorResponse);

        ResponseEntity<PasswordResetResponse> response = passwordResetController.changePassword("", "newPassword");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Email cannot be empty", response.getBody().getStatus());
        verify(passwordResetService).changePassword("", "newPassword");
    }

    @Test
    void changePassword_NullPassword() {  //NOSONAR
        PasswordResetResponse errorResponse = new PasswordResetResponse("Invalid password");
        when(passwordResetService.changePassword(eq("test@example.com"), isNull())).thenReturn(errorResponse);

        ResponseEntity<PasswordResetResponse> response = passwordResetController.changePassword("test@example.com", null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Invalid password", response.getBody().getStatus());
        verify(passwordResetService).changePassword(eq("test@example.com"), isNull());
    }

    @Test
    void changePassword_EmptyPassword() {  //NOSONAR
        PasswordResetResponse errorResponse = new PasswordResetResponse("Password cannot be empty");
        when(passwordResetService.changePassword("test@example.com", "")).thenReturn(errorResponse);

        ResponseEntity<PasswordResetResponse> response = passwordResetController.changePassword("test@example.com", "");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password cannot be empty", response.getBody().getStatus());
        verify(passwordResetService).changePassword("test@example.com", "");
    }

    @Test
    void changePassword_InvalidEmailFormat() {  //NOSONAR
        PasswordResetResponse errorResponse = new PasswordResetResponse("Invalid email format");
        when(passwordResetService.changePassword("invalid-email", "newPassword")).thenReturn(errorResponse);

        ResponseEntity<PasswordResetResponse> response = passwordResetController.changePassword("invalid-email", "newPassword");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Invalid email format", response.getBody().getStatus());
        verify(passwordResetService).changePassword("invalid-email", "newPassword");
    }

    // ============= INTEGRATION AND EDGE CASE TESTS =============

    @Test
    void requestPasswordReset_WithSpecialCharactersInEmail() throws MessagingException {  //NOSONAR
        String specialEmail = "test+special@example.com";
        User specialUser = new User();
        specialUser.setId(2L);
        specialUser.setEmail(specialEmail);

        when(userRepository.findByEmail(specialEmail)).thenReturn(Optional.of(specialUser));
        doNothing().when(passwordResetService).createPasswordResetTokenForUser(any(User.class), anyString());
        doNothing().when(passwordResetService).sendPasswordResetEmail(eq(specialEmail), anyString());

        ResponseEntity<String> response = passwordResetController.requestPasswordReset(specialEmail);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset email sent successfully", response.getBody());
        verify(userRepository).findByEmail(specialEmail);
    }

    @Test
    void resetPassword_WithComplexPassword() {  //NOSONAR
        String complexPassword = "P@ssw0rd!@#$%^&*()_+-=[]{}|;:,.<>?";
        when(passwordResetService.resetPassword("reset-token", complexPassword)).thenReturn(passwordResetResponse);

        ResponseEntity<PasswordResetResponse> response = passwordResetController.resetPassword("reset-token", complexPassword);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset successfully", response.getBody().getStatus());
        verify(passwordResetService).resetPassword("reset-token", complexPassword);
    }

    @Test
    void getPasswordResetCode_MultipleCallsSameEmail() {  //NOSONAR
        when(passwordResetService.getTokenByEmail("test@example.com"))
                .thenReturn("token1")
                .thenReturn("token2");

        String result1 = passwordResetController.getPasswordResetCode("test@example.com");
        String result2 = passwordResetController.getPasswordResetCode("test@example.com");

        assertEquals("token1", result1);
        assertEquals("token2", result2);
        verify(passwordResetService, times(2)).getTokenByEmail("test@example.com");
    }
}