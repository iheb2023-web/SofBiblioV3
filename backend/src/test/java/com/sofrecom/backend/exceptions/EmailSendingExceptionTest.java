package com.sofrecom.backend.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


// Test for EmailSendingException
class EmailSendingExceptionTest {

    @Test
    void testEmailSendingExceptionWithMessageAndCause() {
        // Arrange
        String message = "Failed to send email";
        Throwable cause = new RuntimeException("SMTP server unavailable");

        // Act
        EmailSendingException exception = new EmailSendingException(message, cause);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testEmailSendingExceptionWithNullMessage() {
        // Arrange
        String message = null;
        Throwable cause = new RuntimeException("Network error");

        // Act
        EmailSendingException exception = new EmailSendingException(message, cause);

        // Assert
        assertNull(exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testEmailSendingExceptionWithNullCause() {
        // Arrange
        String message = "Email service is down";
        Throwable cause = null;

        // Act
        EmailSendingException exception = new EmailSendingException(message, cause);

        // Assert
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }
}
