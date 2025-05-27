package com.sofrecom.backend.exceptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class EmailAlreadyExistsExceptionTest {

    @Test
    void testEmailAlreadyExistsExceptionWithMessage() {
        // Arrange
        String message = "Email already exists in the system";

        // Act
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException(message);

        // Assert
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testEmailAlreadyExistsExceptionWithNullMessage() {
        // Arrange
        String message = null;

        // Act
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException(message);

        // Assert
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testEmailAlreadyExistsExceptionWithEmptyMessage() {
        // Arrange
        String message = "";

        // Act
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException(message);

        // Assert
        assertEquals("", exception.getMessage());
        assertNull(exception.getCause());
    }
}
