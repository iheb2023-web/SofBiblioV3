package com.sofrecom.backend.controllers;

import com.sofrecom.backend.dtos.ReviewsDto;
import com.sofrecom.backend.entities.Reviews;
import com.sofrecom.backend.services.IReviewsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewsControllerTest {

    @Mock
    private IReviewsService reviewsService;

    @InjectMocks
    private ReviewsController reviewsController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addReviews_shouldReturnSavedReview() {
        Reviews review = new Reviews();
        when(reviewsService.addReviews(review)).thenReturn(review);

        Reviews result = reviewsController.addReviews(review);

        assertNotNull(result);
        verify(reviewsService).addReviews(review);
    }

    @Test
    void getAllReviews_shouldReturnListOfReviews() {
        Reviews r1 = new Reviews();
        Reviews r2 = new Reviews();
        when(reviewsService.getAllReviews()).thenReturn(Arrays.asList(r1, r2));

        List<Reviews> result = reviewsController.getAllReviews();

        assertEquals(2, result.size());
        verify(reviewsService).getAllReviews();
    }

    @Test
    void getReviewsByIdBook_shouldReturnListOfReviewsDto() {
        Long bookId = 1L;
        ReviewsDto dto1 = new ReviewsDto();
        ReviewsDto dto2 = new ReviewsDto();
        when(reviewsService.getReviewsByIdBook(bookId)).thenReturn(Arrays.asList(dto1, dto2));

        List<ReviewsDto> result = reviewsController.getReviewsByIdBook(bookId);

        assertEquals(2, result.size());
        verify(reviewsService).getReviewsByIdBook(bookId);
    }

    @Test
    void updateReviews_shouldReturnUpdatedReview() {
        Long idReview = 1L;
        Reviews review = new Reviews();
        when(reviewsService.updateReviews(idReview, review)).thenReturn(review);

        Reviews result = reviewsController.updateReviews(idReview, review);

        assertNotNull(result);
        verify(reviewsService).updateReviews(idReview, review);
    }

    @Test
    void deleteReviews_shouldInvokeServiceDelete() {
        Long id = 1L;
        doNothing().when(reviewsService).deleteReviews(id);

        reviewsController.deleteReviews(id);

        verify(reviewsService).deleteReviews(id);
    }

    @Test
    void getReviewById_shouldReturnReview() {
        Long id = 1L;
        Reviews review = new Reviews();
        when(reviewsService.getReviewById(id)).thenReturn(review);

        Reviews result = reviewsController.getReviewById(id);

        assertNotNull(result);
        verify(reviewsService).getReviewById(id);
    }

    // Add these additional test methods to your ReviewsControllerTest class

    @Test
    void addReviews_withNullReview_shouldHandleGracefully() {
        Reviews nullReview = null;
        when(reviewsService.addReviews(nullReview)).thenThrow(new IllegalArgumentException("Review cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> reviewsController.addReviews(nullReview));
        verify(reviewsService).addReviews(nullReview);
    }

    @Test
    void addReviews_withValidData_shouldSetCorrectProperties() {
        Reviews review = new Reviews();
        review.setId(1L);
        review.setRating(5);
        review.setContent("Great book!");

        when(reviewsService.addReviews(any(Reviews.class))).thenReturn(review);

        Reviews result = reviewsController.addReviews(review);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(5, result.getRating());
        assertEquals("Great book!", result.getContent());
        verify(reviewsService).addReviews(review);
    }

    @Test
    void getAllReviews_whenEmpty_shouldReturnEmptyList() {
        when(reviewsService.getAllReviews()).thenReturn(Arrays.asList());

        List<Reviews> result = reviewsController.getAllReviews();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(reviewsService).getAllReviews();
    }

    @Test
    void getAllReviews_whenServiceThrowsException_shouldPropagateException() {
        when(reviewsService.getAllReviews()).thenThrow(new RuntimeException("Database connection error"));

        assertThrows(RuntimeException.class, () -> reviewsController.getAllReviews());
        verify(reviewsService).getAllReviews();
    }

    @Test
    void getReviewsByIdBook_withValidId_shouldReturnCorrectReviews() {
        Long bookId = 1L;
        ReviewsDto dto1 = new ReviewsDto();
        dto1.setId(1L);
        dto1.setRating(5);
        dto1.setContent("Excellent book");

        ReviewsDto dto2 = new ReviewsDto();
        dto2.setId(2L);
        dto2.setRating(4);
        dto2.setContent("Good read");

        when(reviewsService.getReviewsByIdBook(bookId)).thenReturn(Arrays.asList(dto1, dto2));

        List<ReviewsDto> result = reviewsController.getReviewsByIdBook(bookId);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(5, result.get(0).getRating());
        assertEquals("Excellent book", result.get(0).getContent());
        assertEquals(2L, result.get(1).getId());
        assertEquals(4, result.get(1).getRating());
        assertEquals("Good read", result.get(1).getContent());
        verify(reviewsService).getReviewsByIdBook(bookId);
    }

    @Test
    void getReviewsByIdBook_withInvalidId_shouldReturnEmptyList() {
        Long invalidBookId = 999L;
        when(reviewsService.getReviewsByIdBook(invalidBookId)).thenReturn(Arrays.asList());

        List<ReviewsDto> result = reviewsController.getReviewsByIdBook(invalidBookId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(reviewsService).getReviewsByIdBook(invalidBookId);
    }

    @Test
    void getReviewsByIdBook_withNullId_shouldThrowException() {
        Long nullId = null;
        when(reviewsService.getReviewsByIdBook(nullId)).thenThrow(new IllegalArgumentException("Book ID cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> reviewsController.getReviewsByIdBook(nullId));
        verify(reviewsService).getReviewsByIdBook(nullId);
    }

    @Test
    void updateReviews_withValidData_shouldReturnUpdatedReview() {
        Long reviewId = 1L;
        Reviews originalReview = new Reviews();
        originalReview.setId(reviewId);
        originalReview.setRating(3);
        originalReview.setContent("Average book");

        Reviews updatedReview = new Reviews();
        updatedReview.setId(reviewId);
        updatedReview.setRating(5);
        updatedReview.setContent("Actually, it's great!");

        when(reviewsService.updateReviews(reviewId, originalReview)).thenReturn(updatedReview);

        Reviews result = reviewsController.updateReviews(reviewId, originalReview);

        assertNotNull(result);
        assertEquals(reviewId, result.getId());
        assertEquals(5, result.getRating());
        assertEquals("Actually, it's great!", result.getContent());
        verify(reviewsService).updateReviews(reviewId, originalReview);
    }

    @Test
    void updateReviews_withNonExistentId_shouldThrowException() {
        Long nonExistentId = 999L;
        Reviews review = new Reviews();
        when(reviewsService.updateReviews(nonExistentId, review))
                .thenThrow(new RuntimeException("Review not found with id: " + nonExistentId));

        assertThrows(RuntimeException.class, () -> reviewsController.updateReviews(nonExistentId, review));
        verify(reviewsService).updateReviews(nonExistentId, review);
    }

    @Test
    void updateReviews_withNullReview_shouldThrowException() {
        Long reviewId = 1L;
        Reviews nullReview = null;
        when(reviewsService.updateReviews(reviewId, nullReview))
                .thenThrow(new IllegalArgumentException("Review data cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> reviewsController.updateReviews(reviewId, nullReview));
        verify(reviewsService).updateReviews(reviewId, nullReview);
    }

    @Test
    void deleteReviews_withValidId_shouldCallServiceMethod() {
        Long reviewId = 1L;
        doNothing().when(reviewsService).deleteReviews(reviewId);

        assertDoesNotThrow(() -> reviewsController.deleteReviews(reviewId));
        verify(reviewsService).deleteReviews(reviewId);
    }

    @Test
    void deleteReviews_withNonExistentId_shouldThrowException() {
        Long nonExistentId = 999L;
        doThrow(new RuntimeException("Review not found with id: " + nonExistentId))
                .when(reviewsService).deleteReviews(nonExistentId);

        assertThrows(RuntimeException.class, () -> reviewsController.deleteReviews(nonExistentId));
        verify(reviewsService).deleteReviews(nonExistentId);
    }

    @Test
    void deleteReviews_withNullId_shouldThrowException() {
        Long nullId = null;
        doThrow(new IllegalArgumentException("Review ID cannot be null"))
                .when(reviewsService).deleteReviews(nullId);

        assertThrows(IllegalArgumentException.class, () -> reviewsController.deleteReviews(nullId));
        verify(reviewsService).deleteReviews(nullId);
    }

    @Test
    void getReviewById_withValidId_shouldReturnCorrectReview() {
        Long reviewId = 1L;
        Reviews review = new Reviews();
        review.setId(reviewId);
        review.setRating(4);
        review.setContent("Good book overall");

        when(reviewsService.getReviewById(reviewId)).thenReturn(review);

        Reviews result = reviewsController.getReviewById(reviewId);

        assertNotNull(result);
        assertEquals(reviewId, result.getId());
        assertEquals(4, result.getRating());
        assertEquals("Good book overall", result.getContent());
        verify(reviewsService).getReviewById(reviewId);
    }

    @Test
    void getReviewById_withNonExistentId_shouldThrowException() {
        Long nonExistentId = 999L;
        when(reviewsService.getReviewById(nonExistentId))
                .thenThrow(new RuntimeException("Review not found with id: " + nonExistentId));

        assertThrows(RuntimeException.class, () -> reviewsController.getReviewById(nonExistentId));
        verify(reviewsService).getReviewById(nonExistentId);
    }

    @Test
    void getReviewById_withNullId_shouldThrowException() {
        Long nullId = null;
        when(reviewsService.getReviewById(nullId))
                .thenThrow(new IllegalArgumentException("Review ID cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> reviewsController.getReviewById(nullId));
        verify(reviewsService).getReviewById(nullId);
    }

    @Test
    void getReviewsByIdBook_withLargeDataset_shouldHandleCorrectly() {
        Long bookId = 1L;
        List<ReviewsDto> largeReviewList = new ArrayList<>();

        // Create 100 review DTOs
        for (int i = 1; i <= 100; i++) {
            ReviewsDto dto = new ReviewsDto();
            dto.setId((long) i);
            dto.setRating(i % 5 + 1); // Rating between 1-5
            dto.setContent("Review number " + i);
            largeReviewList.add(dto);
        }

        when(reviewsService.getReviewsByIdBook(bookId)).thenReturn(largeReviewList);

        List<ReviewsDto> result = reviewsController.getReviewsByIdBook(bookId);

        assertEquals(100, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(100L, result.get(99).getId());
        verify(reviewsService).getReviewsByIdBook(bookId);
    }

    @Test
    void addReviews_withServiceException_shouldPropagateException() {
        Reviews review = new Reviews();
        when(reviewsService.addReviews(review)).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> reviewsController.addReviews(review));
        verify(reviewsService).addReviews(review);
    }

}
