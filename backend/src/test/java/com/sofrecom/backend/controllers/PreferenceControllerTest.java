package com.sofrecom.backend.controllers;


import com.sofrecom.backend.dtos.PreferenceDto;
import com.sofrecom.backend.entities.Preference;
import com.sofrecom.backend.services.IPreferenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PreferenceControllerTest {

    @Mock
    private IPreferenceService preferenceService;

    @InjectMocks
    private PreferenceController preferenceController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getPreferences_shouldReturnListOfPreferences() {
        Preference pref1 = new Preference();
        Preference pref2 = new Preference();
        List<Preference> preferences = Arrays.asList(pref1, pref2);

        when(preferenceService.getPreferences()).thenReturn(preferences);

        List<Preference> result = preferenceController.getPreferences();

        assertEquals(2, result.size());
        verify(preferenceService).getPreferences();
    }

    @Test
    void addPreference_shouldReturnCreatedPreference() {
        PreferenceDto dto = new PreferenceDto();
        Preference createdPreference = new Preference();

        when(preferenceService.addPreference(dto)).thenReturn(createdPreference);

        Preference result = preferenceController.addPreference(dto);

        assertNotNull(result);
        verify(preferenceService).addPreference(dto);
    }

    @Test
    void getPreferenceByUserId_shouldReturnPreference() {
        Long userId = 1L;
        Preference preference = new Preference();

        when(preferenceService.getPreferenceByUserId(userId)).thenReturn(preference);

        Preference result = preferenceController.getPreferenceByUserId(userId);

        assertNotNull(result);
        verify(preferenceService).getPreferenceByUserId(userId);
    }

    // Add these additional test methods to your PreferenceControllerTest class
// Updated to match the actual Preference entity structure

    @Test
    void getPreferences_whenEmpty_shouldReturnEmptyList() {
        when(preferenceService.getPreferences()).thenReturn(Arrays.asList());

        List<Preference> result = preferenceController.getPreferences();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(preferenceService).getPreferences();
    }

    @Test
    void getPreferences_withValidData_shouldReturnCorrectPreferences() {
        Preference pref1 = new Preference();
        pref1.setId(1L);
        pref1.setPreferredBookLength("Medium");
        pref1.setFavoriteGenres(Arrays.asList("Fiction", "Mystery"));
        pref1.setPreferredLanguages(Arrays.asList("English", "French"));
        pref1.setFavoriteAuthors(Arrays.asList("Agatha Christie", "Arthur Conan Doyle"));
        pref1.setType("Reader");

        Preference pref2 = new Preference();
        pref2.setId(2L);
        pref2.setPreferredBookLength("Long");
        pref2.setFavoriteGenres(Arrays.asList("Science Fiction", "Fantasy"));
        pref2.setPreferredLanguages(Arrays.asList("English"));
        pref2.setFavoriteAuthors(Arrays.asList("Isaac Asimov"));
        pref2.setType("Academic");

        List<Preference> preferences = Arrays.asList(pref1, pref2);
        when(preferenceService.getPreferences()).thenReturn(preferences);

        List<Preference> result = preferenceController.getPreferences();

        assertEquals(2, result.size());

        // Verify first preference
        assertEquals(1L, result.get(0).getId());
        assertEquals("Medium", result.get(0).getPreferredBookLength());
        assertEquals(2, result.get(0).getFavoriteGenres().size());
        assertTrue(result.get(0).getFavoriteGenres().contains("Fiction"));
        assertTrue(result.get(0).getFavoriteGenres().contains("Mystery"));
        assertEquals(2, result.get(0).getPreferredLanguages().size());
        assertTrue(result.get(0).getPreferredLanguages().contains("English"));
        assertTrue(result.get(0).getPreferredLanguages().contains("French"));
        assertEquals("Reader", result.get(0).getType());

        // Verify second preference
        assertEquals(2L, result.get(1).getId());
        assertEquals("Long", result.get(1).getPreferredBookLength());
        assertEquals(2, result.get(1).getFavoriteGenres().size());
        assertTrue(result.get(1).getFavoriteGenres().contains("Science Fiction"));
        assertEquals("Academic", result.get(1).getType());

        verify(preferenceService).getPreferences();
    }

    @Test
    void getPreferences_whenServiceThrowsException_shouldPropagateException() {
        when(preferenceService.getPreferences()).thenThrow(new RuntimeException("Database connection error"));

        assertThrows(RuntimeException.class, () -> preferenceController.getPreferences());
        verify(preferenceService).getPreferences();
    }

    @Test
    void getPreferences_withLargeDataset_shouldHandleCorrectly() {
        List<Preference> largePreferenceList = new java.util.ArrayList<>();

        // Create 50 preferences
        for (int i = 1; i <= 50; i++) {
            Preference pref = new Preference();
            pref.setId((long) i);
            pref.setPreferredBookLength("Length" + i);
            pref.setFavoriteGenres(Arrays.asList("Genre" + i, "Genre" + (i + 100)));
            pref.setPreferredLanguages(Arrays.asList("Language" + i));
            pref.setFavoriteAuthors(Arrays.asList("Author" + i));
            pref.setType("Type" + i);
            largePreferenceList.add(pref);
        }

        when(preferenceService.getPreferences()).thenReturn(largePreferenceList);

        List<Preference> result = preferenceController.getPreferences();

        assertEquals(50, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(50L, result.get(49).getId());
        assertEquals("Length1", result.get(0).getPreferredBookLength());
        assertEquals("Length50", result.get(49).getPreferredBookLength());
        verify(preferenceService).getPreferences();
    }

    @Test
    void addPreference_withValidDto_shouldReturnCreatedPreferenceWithCorrectData() {
        PreferenceDto dto = new PreferenceDto();
        dto.setPreferredBookLength("Short");
        dto.setFavoriteGenres(Arrays.asList("Romance", "Drama"));
        dto.setPreferredLanguages(Arrays.asList("Spanish", "Portuguese"));
        dto.setFavoriteAuthors(Arrays.asList("Gabriel García Márquez", "Isabel Allende"));
        dto.setType("Casual");

        Preference createdPreference = new Preference();
        createdPreference.setId(5L);
        createdPreference.setPreferredBookLength("Short");
        createdPreference.setFavoriteGenres(Arrays.asList("Romance", "Drama"));
        createdPreference.setPreferredLanguages(Arrays.asList("Spanish", "Portuguese"));
        createdPreference.setFavoriteAuthors(Arrays.asList("Gabriel García Márquez", "Isabel Allende"));
        createdPreference.setType("Casual");

        when(preferenceService.addPreference(dto)).thenReturn(createdPreference);

        Preference result = preferenceController.addPreference(dto);

        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("Short", result.getPreferredBookLength());
        assertEquals(2, result.getFavoriteGenres().size());
        assertTrue(result.getFavoriteGenres().contains("Romance"));
        assertTrue(result.getFavoriteGenres().contains("Drama"));
        assertEquals(2, result.getPreferredLanguages().size());
        assertTrue(result.getPreferredLanguages().contains("Spanish"));
        assertTrue(result.getPreferredLanguages().contains("Portuguese"));
        assertEquals(2, result.getFavoriteAuthors().size());
        assertTrue(result.getFavoriteAuthors().contains("Gabriel García Márquez"));
        assertEquals("Casual", result.getType());
        verify(preferenceService).addPreference(dto);
    }

    @Test
    void addPreference_withNullDto_shouldThrowException() {
        PreferenceDto nullDto = null;
        when(preferenceService.addPreference(nullDto))
                .thenThrow(new IllegalArgumentException("Preference data cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> preferenceController.addPreference(nullDto));
        verify(preferenceService).addPreference(nullDto);
    }

    @Test
    void addPreference_withEmptyCollections_shouldHandleCorrectly() {
        PreferenceDto dto = new PreferenceDto();
        dto.setPreferredBookLength("Medium");
        dto.setFavoriteGenres(Arrays.asList());
        dto.setPreferredLanguages(Arrays.asList());
        dto.setFavoriteAuthors(Arrays.asList());
        dto.setType("Minimal");

        Preference createdPreference = new Preference();
        createdPreference.setId(1L);
        createdPreference.setPreferredBookLength("Medium");
        createdPreference.setFavoriteGenres(Arrays.asList());
        createdPreference.setPreferredLanguages(Arrays.asList());
        createdPreference.setFavoriteAuthors(Arrays.asList());
        createdPreference.setType("Minimal");

        when(preferenceService.addPreference(dto)).thenReturn(createdPreference);

        Preference result = preferenceController.addPreference(dto);

        assertNotNull(result);
        assertEquals("Medium", result.getPreferredBookLength());
        assertTrue(result.getFavoriteGenres().isEmpty());
        assertTrue(result.getPreferredLanguages().isEmpty());
        assertTrue(result.getFavoriteAuthors().isEmpty());
        assertEquals("Minimal", result.getType());
        verify(preferenceService).addPreference(dto);
    }

    @Test
    void addPreference_withNullCollections_shouldThrowException() {
        PreferenceDto dto = new PreferenceDto();
        dto.setPreferredBookLength("Long");
        dto.setFavoriteGenres(null);
        dto.setPreferredLanguages(null);
        dto.setFavoriteAuthors(null);
        dto.setType("Invalid");

        when(preferenceService.addPreference(dto))
                .thenThrow(new IllegalArgumentException("Collections cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> preferenceController.addPreference(dto));
        verify(preferenceService).addPreference(dto);
    }

    @Test
    void addPreference_whenServiceThrowsException_shouldPropagateException() {
        PreferenceDto dto = new PreferenceDto();
        dto.setPreferredBookLength("Medium");

        when(preferenceService.addPreference(dto))
                .thenThrow(new RuntimeException("Database error while saving preference"));

        assertThrows(RuntimeException.class, () -> preferenceController.addPreference(dto));
        verify(preferenceService).addPreference(dto);
    }

    @Test
    void getPreferenceByUserId_withValidId_shouldReturnCorrectPreference() {
        Long userId = 25L;
        Preference preference = new Preference();
        preference.setId(3L);
        preference.setPreferredBookLength("Long");
        preference.setFavoriteGenres(Arrays.asList("Historical Fiction", "Biography"));
        preference.setPreferredLanguages(Arrays.asList("Italian", "German"));
        preference.setFavoriteAuthors(Arrays.asList("Umberto Eco", "Thomas Mann"));
        preference.setType("Scholar");

        when(preferenceService.getPreferenceByUserId(userId)).thenReturn(preference);

        Preference result = preferenceController.getPreferenceByUserId(userId);

        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("Long", result.getPreferredBookLength());
        assertEquals(2, result.getFavoriteGenres().size());
        assertTrue(result.getFavoriteGenres().contains("Historical Fiction"));
        assertTrue(result.getFavoriteGenres().contains("Biography"));
        assertEquals(2, result.getPreferredLanguages().size());
        assertTrue(result.getPreferredLanguages().contains("Italian"));
        assertTrue(result.getPreferredLanguages().contains("German"));
        assertEquals(2, result.getFavoriteAuthors().size());
        assertTrue(result.getFavoriteAuthors().contains("Umberto Eco"));
        assertTrue(result.getFavoriteAuthors().contains("Thomas Mann"));
        assertEquals("Scholar", result.getType());
        verify(preferenceService).getPreferenceByUserId(userId);
    }

    @Test
    void getPreferenceByUserId_withNonExistentUserId_shouldThrowException() {
        Long nonExistentUserId = 999L;
        when(preferenceService.getPreferenceByUserId(nonExistentUserId))
                .thenThrow(new RuntimeException("No preference found for user ID: " + nonExistentUserId));

        assertThrows(RuntimeException.class, () -> preferenceController.getPreferenceByUserId(nonExistentUserId));
        verify(preferenceService).getPreferenceByUserId(nonExistentUserId);
    }

    @Test
    void getPreferenceByUserId_withNullId_shouldThrowException() {
        Long nullId = null;
        when(preferenceService.getPreferenceByUserId(nullId))
                .thenThrow(new IllegalArgumentException("User ID cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> preferenceController.getPreferenceByUserId(nullId));
        verify(preferenceService).getPreferenceByUserId(nullId);
    }

    @Test
    void getPreferenceByUserId_withNegativeId_shouldThrowException() {
        Long negativeId = -1L;
        when(preferenceService.getPreferenceByUserId(negativeId))
                .thenThrow(new IllegalArgumentException("User ID must be positive"));

        assertThrows(IllegalArgumentException.class, () -> preferenceController.getPreferenceByUserId(negativeId));
        verify(preferenceService).getPreferenceByUserId(negativeId);
    }

    @Test
    void getPreferenceByUserId_whenServiceThrowsException_shouldPropagateException() {
        Long userId = 1L;
        when(preferenceService.getPreferenceByUserId(userId))
                .thenThrow(new RuntimeException("Database connection timeout"));

        assertThrows(RuntimeException.class, () -> preferenceController.getPreferenceByUserId(userId));
        verify(preferenceService).getPreferenceByUserId(userId);
    }

    @Test
    void addPreference_withSingleItemCollections_shouldHandleCorrectly() {
        PreferenceDto dto = new PreferenceDto();
        dto.setPreferredBookLength("Short");
        dto.setFavoriteGenres(Arrays.asList("Poetry"));
        dto.setPreferredLanguages(Arrays.asList("Japanese"));
        dto.setFavoriteAuthors(Arrays.asList("Haruki Murakami"));
        dto.setType("Minimalist");

        Preference createdPreference = new Preference();
        createdPreference.setId(1L);
        createdPreference.setPreferredBookLength("Short");
        createdPreference.setFavoriteGenres(Arrays.asList("Poetry"));
        createdPreference.setPreferredLanguages(Arrays.asList("Japanese"));
        createdPreference.setFavoriteAuthors(Arrays.asList("Haruki Murakami"));
        createdPreference.setType("Minimalist");

        when(preferenceService.addPreference(dto)).thenReturn(createdPreference);

        Preference result = preferenceController.addPreference(dto);

        assertNotNull(result);
        assertEquals(1, result.getFavoriteGenres().size());
        assertEquals("Poetry", result.getFavoriteGenres().get(0));
        assertEquals(1, result.getPreferredLanguages().size());
        assertEquals("Japanese", result.getPreferredLanguages().get(0));
        assertEquals(1, result.getFavoriteAuthors().size());
        assertEquals("Haruki Murakami", result.getFavoriteAuthors().get(0));
        verify(preferenceService).addPreference(dto);
    }

    @Test
    void addPreference_withLargeCollections_shouldHandleCorrectly() {
        PreferenceDto dto = new PreferenceDto();
        dto.setPreferredBookLength("Any");
        dto.setFavoriteGenres(Arrays.asList("Fiction", "Non-Fiction", "Mystery", "Romance", "Sci-Fi", "Fantasy", "Biography", "History"));
        dto.setPreferredLanguages(Arrays.asList("English", "Spanish", "French", "German", "Italian"));
        dto.setFavoriteAuthors(Arrays.asList("Author1", "Author2", "Author3", "Author4", "Author5"));
        dto.setType("Eclectic");

        Preference createdPreference = new Preference();
        createdPreference.setId(1L);
        createdPreference.setPreferredBookLength("Any");
        createdPreference.setFavoriteGenres(dto.getFavoriteGenres());
        createdPreference.setPreferredLanguages(dto.getPreferredLanguages());
        createdPreference.setFavoriteAuthors(dto.getFavoriteAuthors());
        createdPreference.setType("Eclectic");

        when(preferenceService.addPreference(dto)).thenReturn(createdPreference);

        Preference result = preferenceController.addPreference(dto);

        assertNotNull(result);
        assertEquals(8, result.getFavoriteGenres().size());
        assertEquals(5, result.getPreferredLanguages().size());
        assertEquals(5, result.getFavoriteAuthors().size());
        assertTrue(result.getFavoriteGenres().contains("Fiction"));
        assertTrue(result.getFavoriteGenres().contains("History"));
        assertTrue(result.getPreferredLanguages().contains("English"));
        assertTrue(result.getPreferredLanguages().contains("Italian"));
        assertEquals("Eclectic", result.getType());
        verify(preferenceService).addPreference(dto);
    }

    @Test
    void getPreferences_shouldMaintainOrderFromService() {
        Preference pref1 = new Preference();
        pref1.setId(1L);
        pref1.setType("First");

        Preference pref2 = new Preference();
        pref2.setId(2L);
        pref2.setType("Second");

        Preference pref3 = new Preference();
        pref3.setId(3L);
        pref3.setType("Third");

        List<Preference> orderedPreferences = Arrays.asList(pref1, pref2, pref3);
        when(preferenceService.getPreferences()).thenReturn(orderedPreferences);

        List<Preference> result = preferenceController.getPreferences();

        assertEquals(3, result.size());
        assertEquals("First", result.get(0).getType());
        assertEquals("Second", result.get(1).getType());
        assertEquals("Third", result.get(2).getType());
        verify(preferenceService).getPreferences();
    }
}
