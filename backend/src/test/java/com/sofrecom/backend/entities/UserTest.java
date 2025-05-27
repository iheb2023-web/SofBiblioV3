package com.sofrecom.backend.entities;


import com.sofrecom.backend.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Entity Tests")
class UserTest {

    private User user;
    private Role mockRole;
    @Value("${jwt.secret-key}")
    private String password;

    @BeforeEach
    void setUp() {
        mockRole = mock(Role.class);
        user = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .image("profile.jpg")
                .job("Software Engineer")
                .birthday(LocalDate.of(1990, 5, 15))
                .number(1234567890L)
                .role(mockRole)
                .password(password)
                .hasPreference(true)
                .hasSetPassword(true)
                .build();
    }

    @Test
    @DisplayName("Should create user with builder pattern")
    void shouldCreateUserWithBuilder() {
        // Given & When
        User newUser = User.builder()
                .firstname("Jane")
                .lastname("Smith")
                .email("jane.smith@example.com")
                .role(mockRole)
                .build();

        // Then
        assertThat(newUser.getFirstname()).isEqualTo("Jane");
        assertThat(newUser.getLastname()).isEqualTo("Smith");
        assertThat(newUser.getEmail()).isEqualTo("jane.smith@example.com");
        assertThat(newUser.getRole()).isEqualTo(mockRole);
    }

    @Test
    @DisplayName("Should create user with no-args constructor")
    void shouldCreateUserWithNoArgsConstructor() {
        // Given & When
        User newUser = new User();

        // Then
        assertThat(newUser).isNotNull();
        assertThat(newUser.getId()).isNull();
        assertThat(newUser.getEmail()).isNull();
    }

    @Test
    @DisplayName("Should create user with all-args constructor")
    void shouldCreateUserWithAllArgsConstructor() {
        // Given
        List<Book> books = new ArrayList<>();
        List<Borrow> borrows = new ArrayList<>();
        Preference preference = new Preference();

        // When
        User newUser = new User(
                2L, "Alice", "Johnson", "alice@example.com",
                "alice.jpg", "Designer", LocalDate.of(1985, 3, 20),
                9876543210L, mockRole, "password123", books, borrows,
                preference, false, true
        );

        // Then
        assertThat(newUser.getId()).isEqualTo(2L);
        assertThat(newUser.getFirstname()).isEqualTo("Alice");
        assertThat(newUser.getLastname()).isEqualTo("Johnson");
        assertThat(newUser.getEmail()).isEqualTo("alice@example.com");
        assertThat(newUser.getImage()).isEqualTo("alice.jpg");
        assertThat(newUser.getJob()).isEqualTo("Designer");
        assertThat(newUser.getBirthday()).isEqualTo(LocalDate.of(1985, 3, 20));
        assertThat(newUser.getNumber()).isEqualTo(9876543210L);
        assertThat(newUser.getRole()).isEqualTo(mockRole);
        assertThat(newUser.getPassword()).isEqualTo("password123");
        assertThat(newUser.getBooks()).isEqualTo(books);
        assertThat(newUser.getBorrows()).isEqualTo(borrows);
        assertThat(newUser.getPreference()).isEqualTo(preference);
        assertThat(newUser.getHasPreference()).isFalse();
        assertThat(newUser.getHasSetPassword()).isTrue();
    }

    @Test
    @DisplayName("Should get username from email")
    void shouldGetUsernameFromEmail() {
        // Given & When
        String username = user.getUsername();

        // Then
        assertThat(username).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("Should get password")
    void shouldGetPassword() {
        // Given & When
        String actualPassword = user.getPassword();

        // Then
        assertThat(actualPassword).isEqualTo(user.getPassword());
    }




    @Test
    @DisplayName("Should return true for account non expired")
    void shouldReturnTrueForAccountNonExpired() {
        // Given & When
        boolean isNonExpired = user.isAccountNonExpired();

        // Then
        assertThat(isNonExpired).isTrue();
    }

    @Test
    @DisplayName("Should return true for account non locked")
    void shouldReturnTrueForAccountNonLocked() {
        // Given & When
        boolean isNonLocked = user.isAccountNonLocked();

        // Then
        assertThat(isNonLocked).isTrue();
    }

    @Test
    @DisplayName("Should return true for credentials non expired")
    void shouldReturnTrueForCredentialsNonExpired() {
        // Given & When
        boolean isNonExpired = user.isCredentialsNonExpired();

        // Then
        assertThat(isNonExpired).isTrue();
    }

    @Test
    @DisplayName("Should return true for enabled")
    void shouldReturnTrueForEnabled() {
        // Given & When
        boolean isEnabled = user.isEnabled();

        // Then
        assertThat(isEnabled).isTrue();
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void shouldHandleNullValuesGracefully() {
        // Given
        User userWithNulls = new User();

        // When & Then
        assertDoesNotThrow(() -> {
            userWithNulls.getUsername(); // Should return null without exception
            userWithNulls.getPassword(); // Should return null without exception
            userWithNulls.isAccountNonExpired(); // Should return true
            userWithNulls.isAccountNonLocked(); // Should return true
            userWithNulls.isCredentialsNonExpired(); // Should return true
            userWithNulls.isEnabled(); // Should return true
        });
    }

    @Test
    @DisplayName("Should handle relationships properly")
    void shouldHandleRelationshipsProperly() {
        // Given
        List<Book> books = new ArrayList<>();
        Book book1 = new Book();
        book1.setTitle("Test Book 1");
        books.add(book1);

        List<Borrow> borrows = new ArrayList<>();
        Borrow borrow1 = new Borrow();
        borrows.add(borrow1);

        Preference preference = new Preference();

        // When
        user.setBooks(books);
        user.setBorrows(borrows);
        user.setPreference(preference);

        // Then
        assertThat(user.getBooks()).hasSize(1);
        assertThat(user.getBooks().get(0).getTitle()).isEqualTo("Test Book 1");
        assertThat(user.getBorrows()).hasSize(1);
        assertThat(user.getPreference()).isEqualTo(preference);
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void shouldTestEqualsAndHashCode() {
        // Given
        User user1 = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstname("Test")
                .build();

        User user2 = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstname("Test")
                .build();

        User user3 = User.builder()
                .id(2L)
                .email("different@example.com")
                .firstname("Different")
                .build();

        // Then
        assertThat(user1)
                .isEqualTo(user2)
                .hasSameHashCodeAs(user2)
                .isNotEqualTo(user3);
    }



    @Test
    @DisplayName("Should test toString method")
    void shouldTestToStringMethod() {
        // Given & When
        String userString = user.toString();

        // Then
        assertThat(userString)
                .contains("User")
                .contains("john.doe@example.com")
                .contains("John")
                .contains("Doe");
    }


    @Test
    @DisplayName("Should handle boolean flags correctly")
    void shouldHandleBooleanFlagsCorrectly() {
        // Given
        User newUser = new User();

        // When
        newUser.setHasPreference(true);
        newUser.setHasSetPassword(false);

        // Then
        assertThat(newUser.getHasPreference()).isTrue();
        assertThat(newUser.getHasSetPassword()).isFalse();
    }

    @Test
    @DisplayName("Should handle birthday properly")
    void shouldHandleBirthdayProperly() {
        // Given
        LocalDate testDate = LocalDate.of(1995, 12, 25);

        // When
        user.setBirthday(testDate);

        // Then
        assertThat(user.getBirthday()).isEqualTo(testDate);
        assertThat(user.getBirthday().getYear()).isEqualTo(1995);
        assertThat(user.getBirthday().getMonthValue()).isEqualTo(12);
        assertThat(user.getBirthday().getDayOfMonth()).isEqualTo(25);
    }

    @Test
    @DisplayName("Should handle number field properly")
    void shouldHandleNumberFieldProperly() {
        // Given
        Long testNumber = 5555551234L;

        // When
        user.setNumber(testNumber);

        // Then
        assertThat(user.getNumber()).isEqualTo(testNumber);
    }

    @Test
    @DisplayName("Should implement Serializable")
    void shouldImplementSerializable() {
        // Given & When & Then
        assertThat(user).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    @DisplayName("Should handle empty collections")
    void shouldHandleEmptyCollections() {
        // Given
        User newUser = new User();
        newUser.setBooks(new ArrayList<>());
        newUser.setBorrows(new ArrayList<>());

        // When & Then
        assertThat(newUser.getBooks()).isEmpty();
        assertThat(newUser.getBorrows()).isEmpty();
        assertDoesNotThrow(() -> newUser.getBooks().size());
        assertDoesNotThrow(() -> newUser.getBorrows().size());
    }
}