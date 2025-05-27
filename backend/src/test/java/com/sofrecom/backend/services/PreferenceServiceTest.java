package com.sofrecom.backend.services;

import com.sofrecom.backend.dtos.PreferenceDto;
import com.sofrecom.backend.entities.Preference;
import com.sofrecom.backend.entities.User;
import com.sofrecom.backend.repositories.PreferenceRepository;
import com.sofrecom.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreferenceServiceTest {

    @Mock
    private PreferenceRepository preferenceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PreferenceService preferenceService;

    private User user;
    private Preference preference;
    private PreferenceDto preferenceDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        preference = Preference.builder()
                .id(1L)
                .preferredBookLength("Short")
                .favoriteGenres(Arrays.asList("Fiction", "Mystery"))
                .preferredLanguages(Arrays.asList("English"))
                .favoriteAuthors(Arrays.asList("Author A"))
                .type("Book")
                .user(user)
                .build();

        preferenceDto = new PreferenceDto();
        preferenceDto.setUserId(1L);
        preferenceDto.setPreferredBookLength("Short");
        preferenceDto.setFavoriteGenres(Arrays.asList("Fiction", "Mystery"));
        preferenceDto.setPreferredLanguages(Arrays.asList("English"));
        preferenceDto.setFavoriteAuthors(Arrays.asList("Author A"));
        preferenceDto.setType("Book");
    }

    @Test
    void getPreferences_ShouldReturnAllPreferences() {  //NOSONAR
        List<Preference> preferences = Arrays.asList(preference);
        when(preferenceRepository.findAll()).thenReturn(preferences);

        List<Preference> result = preferenceService.getPreferences();

        assertEquals(1, result.size());
        assertEquals(preference, result.get(0));
        verify(preferenceRepository).findAll();
    }

    @Test
    void getPreferences_EmptyList_ShouldReturnEmptyList() {  //NOSONAR
        when(preferenceRepository.findAll()).thenReturn(Collections.emptyList());

        List<Preference> result = preferenceService.getPreferences();

        assertTrue(result.isEmpty());
        verify(preferenceRepository).findAll();
    }

    @Test
    void addPreference_ValidUser_ShouldSavePreference() {  //NOSONAR
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(preferenceRepository.save(any(Preference.class))).thenReturn(preference);

        Preference result = preferenceService.addPreference(preferenceDto);

        assertNotNull(result);
        assertEquals("Short", result.getPreferredBookLength());
        assertEquals(Arrays.asList("Fiction", "Mystery"), result.getFavoriteGenres());
        assertEquals(Arrays.asList("English"), result.getPreferredLanguages());
        assertEquals(Arrays.asList("Author A"), result.getFavoriteAuthors());
        assertEquals("Book", result.getType());
        assertEquals(user, result.getUser());
        verify(userRepository).findById(1L);
        verify(preferenceRepository).save(any(Preference.class));
    }

    @Test
    void addPreference_NonExistingUser_ShouldSavePreferenceWithNullUser() {  //NOSONAR
        Preference preferenceWithNullUser = Preference.builder()
                .id(1L)
                .preferredBookLength("Short")
                .favoriteGenres(Arrays.asList("Fiction", "Mystery"))
                .preferredLanguages(Arrays.asList("English"))
                .favoriteAuthors(Arrays.asList("Author A"))
                .type("Book")
                .user(null)
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(preferenceRepository.save(any(Preference.class))).thenReturn(preferenceWithNullUser);

        Preference result = preferenceService.addPreference(preferenceDto);

        assertNotNull(result);
        assertNull(result.getUser());
        assertEquals("Short", result.getPreferredBookLength());
        assertEquals(Arrays.asList("Fiction", "Mystery"), result.getFavoriteGenres());
        verify(userRepository).findById(1L);
        verify(preferenceRepository).save(any(Preference.class));
    }

    @Test
    void addPreference_NullPreferenceDto_ShouldThrowException() { //NOSONAR
        assertThrows(NullPointerException.class, () -> preferenceService.addPreference(null));
        verify(userRepository, never()).findById(any());
        verify(preferenceRepository, never()).save(any());
    }

    @Test
    void addPreference_PartialPreferenceDto_ShouldSavePreferenceWithNullFields() { //NOSONAR
        PreferenceDto partialDto = new PreferenceDto();
        partialDto.setUserId(1L);
        partialDto.setPreferredBookLength("Medium"); // Other fields are null

        Preference partialPreference = Preference.builder()
                .id(2L)
                .preferredBookLength("Medium")
                .favoriteGenres(null)
                .preferredLanguages(null)
                .favoriteAuthors(null)
                .type(null)
                .user(user)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(preferenceRepository.save(any(Preference.class))).thenReturn(partialPreference);

        Preference result = preferenceService.addPreference(partialDto);

        assertNotNull(result);
        assertEquals("Medium", result.getPreferredBookLength());
        assertNull(result.getFavoriteGenres());
        assertNull(result.getPreferredLanguages());
        assertNull(result.getFavoriteAuthors());
        assertNull(result.getType());
        assertEquals(user, result.getUser());
        verify(userRepository).findById(1L);
        verify(preferenceRepository).save(any(Preference.class));
    }

    @Test
    void addPreference_NegativeUserId_ShouldSavePreferenceWithNullUser() { //NOSONAR
        PreferenceDto invalidDto = new PreferenceDto();
        invalidDto.setUserId(-1L);
        invalidDto.setPreferredBookLength("Short");
        invalidDto.setFavoriteGenres(Arrays.asList("Fiction"));
        invalidDto.setPreferredLanguages(Arrays.asList("English"));
        invalidDto.setFavoriteAuthors(Arrays.asList("Author B"));
        invalidDto.setType("Book");

        Preference preferenceWithNullUser = Preference.builder()
                .id(3L)
                .preferredBookLength("Short")
                .favoriteGenres(Arrays.asList("Fiction"))
                .preferredLanguages(Arrays.asList("English"))
                .favoriteAuthors(Arrays.asList("Author B"))
                .type("Book")
                .user(null)
                .build();

        when(userRepository.findById(-1L)).thenReturn(Optional.empty());
        when(preferenceRepository.save(any(Preference.class))).thenReturn(preferenceWithNullUser);

        Preference result = preferenceService.addPreference(invalidDto);

        assertNotNull(result);
        assertNull(result.getUser());
        assertEquals("Short", result.getPreferredBookLength());
        assertEquals(Arrays.asList("Fiction"), result.getFavoriteGenres());
        verify(userRepository).findById(-1L);
        verify(preferenceRepository).save(any(Preference.class));
    }

    @Test
    void getPreferenceByUserId_ExistingPreference_ShouldReturnPreference() { //NOSONAR
        when(preferenceRepository.findPreferenceByUserId(1L)).thenReturn(preference);

        Preference result = preferenceService.getPreferenceByUserId(1L);

        assertNotNull(result);
        assertEquals(preference, result);
        verify(preferenceRepository).findPreferenceByUserId(1L);
    }

    @Test
    void getPreferenceByUserId_NonExistingPreference_ShouldReturnNull() { //NOSONAR
        when(preferenceRepository.findPreferenceByUserId(1L)).thenReturn(null);

        Preference result = preferenceService.getPreferenceByUserId(1L);

        assertNull(result);
        verify(preferenceRepository).findPreferenceByUserId(1L);
    }

    @Test
    void getPreferenceByUserId_NegativeId_ShouldReturnNull() { //NOSONAR
        when(preferenceRepository.findPreferenceByUserId(-1L)).thenReturn(null);

        Preference result = preferenceService.getPreferenceByUserId(-1L);

        assertNull(result);
        verify(preferenceRepository).findPreferenceByUserId(-1L);
    }

    @Test
    void getPreferenceByUserId_NullId_ShouldThrowException() { //NOSONAR
        when(preferenceRepository.findPreferenceByUserId(null)).thenThrow(new IllegalArgumentException("ID cannot be null"));
        assertThrows(IllegalArgumentException.class, () -> preferenceService.getPreferenceByUserId(null));
        verify(preferenceRepository).findPreferenceByUserId(null);
    }
}