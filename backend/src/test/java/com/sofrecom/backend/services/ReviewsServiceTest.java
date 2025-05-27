package com.sofrecom.backend.services;

import com.sofrecom.backend.dtos.ReviewsDto;
import com.sofrecom.backend.entities.Reviews;
import com.sofrecom.backend.entities.User;
import com.sofrecom.backend.repositories.ReviewsRepository;
import com.sofrecom.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewsServiceTest {

    @Mock
    private ReviewsRepository reviewsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SocketIOService socketIOService;

    @InjectMocks
    private ReviewsService reviewsService;

    private User user;
    private Reviews review;
    private ReviewsDto reviewsDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        review = new Reviews();
        review.setId(1L);
        review.setUser(user);
        review.setContent("Great book!");
        review.setRating(5);
        review.setCreatedAt(LocalDateTime.now());

        reviewsDto = new ReviewsDto();
        reviewsDto.setContent("Great book!");
        reviewsDto.setRating(5);
    }

    @Test
    void addReviews_ValidUser_ShouldSaveReviewAndSendNotification() { //NOSONAR
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(reviewsRepository.save(any(Reviews.class))).thenReturn(review);
        doNothing().when(socketIOService).sendAddReviewNotification(anyLong());

        Reviews result = reviewsService.addReviews(review);

        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals("Great book!", result.getContent());
        assertNotNull(result.getCreatedAt());
        verify(userRepository).findByEmail("test@example.com");
        verify(reviewsRepository).save(any(Reviews.class));
        verify(socketIOService).sendAddReviewNotification(review.getId());
    }

    @Test
    void addReviews_NonExistingUser_ShouldSaveReviewWithNullUser() { //NOSONAR
        Reviews reviewWithNullUser = new Reviews();
        reviewWithNullUser.setId(1L);
        reviewWithNullUser.setUser(null);
        reviewWithNullUser.setContent("Great book!");
        reviewWithNullUser.setRating(5);
        reviewWithNullUser.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(reviewsRepository.save(any(Reviews.class))).thenReturn(reviewWithNullUser);
        doNothing().when(socketIOService).sendAddReviewNotification(anyLong());

        Reviews result = reviewsService.addReviews(review);

        assertNotNull(result);
        assertNull(result.getUser());
        assertEquals("Great book!", result.getContent());
        assertNotNull(result.getCreatedAt());
        verify(userRepository).findByEmail("test@example.com");
        verify(reviewsRepository).save(any(Reviews.class));
        verify(socketIOService).sendAddReviewNotification(reviewWithNullUser.getId());
    }

    @Test
    void addReviews_NullReview_ShouldThrowException() { //NOSONAR
        assertThrows(NullPointerException.class, () -> reviewsService.addReviews(null));
        verify(userRepository, never()).findByEmail(any());
        verify(reviewsRepository, never()).save(any());
        verify(socketIOService, never()).sendAddReviewNotification(anyLong());
    }

    @Test
    void addReviews_EmptyUserEmail_ShouldSaveReviewWithNullUser() { //NOSONAR
        Reviews reviewWithEmptyEmail = new Reviews();
        reviewWithEmptyEmail.setUser(new User());
        reviewWithEmptyEmail.getUser().setEmail("");
        reviewWithEmptyEmail.setContent("Good book!");
        reviewWithEmptyEmail.setRating(4);

        Reviews savedReview = new Reviews();
        savedReview.setId(2L);
        savedReview.setUser(null);
        savedReview.setContent("Good book!");
        savedReview.setRating(4);
        savedReview.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByEmail("")).thenReturn(Optional.empty());
        when(reviewsRepository.save(any(Reviews.class))).thenReturn(savedReview);
        doNothing().when(socketIOService).sendAddReviewNotification(anyLong());

        Reviews result = reviewsService.addReviews(reviewWithEmptyEmail);

        assertNotNull(result);
        assertNull(result.getUser());
        assertEquals("Good book!", result.getContent());
        assertEquals(4, result.getRating());
        assertNotNull(result.getCreatedAt());
        verify(userRepository).findByEmail("");
        verify(reviewsRepository).save(any(Reviews.class));
        verify(socketIOService).sendAddReviewNotification(savedReview.getId());
    }

    @Test
    void getAllReviews_ShouldReturnAllReviews() { //NOSONAR
        List<Reviews> reviews = Arrays.asList(review);
        when(reviewsRepository.findAll()).thenReturn(reviews);

        List<Reviews> result = reviewsService.getAllReviews();

        assertEquals(1, result.size());
        assertEquals(review, result.get(0));
        verify(reviewsRepository).findAll();
    }

    @Test
    void getAllReviews_EmptyList_ShouldReturnEmptyList() {//NOSONAR
        when(reviewsRepository.findAll()).thenReturn(Collections.emptyList());

        List<Reviews> result = reviewsService.getAllReviews();

        assertTrue(result.isEmpty());
        verify(reviewsRepository).findAll();
    }

    @Test
    void getReviewsByIdBook_ShouldReturnReviews() { //NOSONAR
        List<ReviewsDto> reviewsDtos = Arrays.asList(reviewsDto);
        when(reviewsRepository.getReviewsByIdBook(1L)).thenReturn(reviewsDtos);

        List<ReviewsDto> result = reviewsService.getReviewsByIdBook(1L);

        assertEquals(1, result.size());
        assertEquals(reviewsDto, result.get(0));
        verify(reviewsRepository).getReviewsByIdBook(1L);
    }

    @Test
    void getReviewsByIdBook_NoReviews_ShouldReturnEmptyList() { //NOSONAR
        when(reviewsRepository.getReviewsByIdBook(1L)).thenReturn(Collections.emptyList());

        List<ReviewsDto> result = reviewsService.getReviewsByIdBook(1L);

        assertTrue(result.isEmpty());
        verify(reviewsRepository).getReviewsByIdBook(1L);
    }

    @Test
    void updateReviews_ExistingReview_ShouldUpdateAndSave() { //NOSONAR
        Reviews updatedReview = new Reviews();
        updatedReview.setContent("Updated content");
        updatedReview.setRating(4);

        when(reviewsRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewsRepository.save(any(Reviews.class))).thenReturn(review);

        Reviews result = reviewsService.updateReviews(1L, updatedReview);

        assertNotNull(result);
        assertEquals("Updated content", result.getContent());
        assertEquals(4, result.getRating());
        assertNotNull(result.getUpdatedAt());
        verify(reviewsRepository).findById(1L);
        verify(reviewsRepository).save(any(Reviews.class));
    }

    @Test
    void updateReviews_NonExistingReview_ShouldThrowAssertionError() { //NOSONAR
        Reviews updatedReview = new Reviews();
        when(reviewsRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AssertionError.class, () -> reviewsService.updateReviews(1L, updatedReview));

        verify(reviewsRepository).findById(1L);
        verify(reviewsRepository, never()).save(any());
    }

    @Test
    void updateReviews_NullReview_ShouldThrowException() { //NOSONAR
        when(reviewsRepository.findById(1L)).thenReturn(Optional.of(review));

        assertThrows(NullPointerException.class, () -> reviewsService.updateReviews(1L, null));

        verify(reviewsRepository).findById(1L);
        verify(reviewsRepository, never()).save(any());
    }

    @Test
    void updateReviews_PartialUpdate_ShouldUpdateNonNullFields() { //NOSONAR
        Reviews updatedReview = new Reviews();
        updatedReview.setContent("Updated content"); // Rating is null

        Reviews savedReview = new Reviews();
        savedReview.setId(1L);
        savedReview.setUser(user);
        savedReview.setContent("Updated content");
        savedReview.setRating(5); // Should retain original rating
        savedReview.setCreatedAt(review.getCreatedAt());
        savedReview.setUpdatedAt(LocalDateTime.now());

        when(reviewsRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewsRepository.save(any(Reviews.class))).thenReturn(savedReview);

        Reviews result = reviewsService.updateReviews(1L, updatedReview);

        assertNotNull(result);
        assertEquals("Updated content", result.getContent());
        assertEquals(5, result.getRating()); // Original rating preserved
        assertNotNull(result.getUpdatedAt());
        verify(reviewsRepository).findById(1L);
        verify(reviewsRepository).save(any(Reviews.class));
    }

    @Test
    void deleteReviews_ShouldDeleteById() { //NOSONAR
        doNothing().when(reviewsRepository).deleteById(1L);

        reviewsService.deleteReviews(1L);

        verify(reviewsRepository).deleteById(1L);
    }

    @Test
    void deleteReviews_NonExistingId_ShouldNotThrowException() { //NOSONAR
        doNothing().when(reviewsRepository).deleteById(999L);

        reviewsService.deleteReviews(999L);

        verify(reviewsRepository).deleteById(999L);
    }

    @Test
    void getReviewById_ExistingReview_ShouldReturnReview() { //NOSONAR
        when(reviewsRepository.findById(1L)).thenReturn(Optional.of(review));

        Reviews result = reviewsService.getReviewById(1L);

        assertNotNull(result);
        assertEquals(review, result);
        verify(reviewsRepository).findById(1L);
    }

    @Test
    void getReviewById_NonExistingReview_ShouldReturnNull() { //NOSONAR
        when(reviewsRepository.findById(1L)).thenReturn(Optional.empty());

        Reviews result = reviewsService.getReviewById(1L);

        assertNull(result);
        verify(reviewsRepository).findById(1L);
    }

    @Test
    void getReviewById_NegativeId_ShouldReturnNull() { //NOSONAR
        when(reviewsRepository.findById(-1L)).thenReturn(Optional.empty());

        Reviews result = reviewsService.getReviewById(-1L);

        assertNull(result);
        verify(reviewsRepository).findById(-1L);
    }
}