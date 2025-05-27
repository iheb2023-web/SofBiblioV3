package com.sofrecom.backend.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    private MockMultipartFile multipartFile;

    @BeforeEach
    void setUp() {
        // Create a sample multipart file for testing
        multipartFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }

    @Test
    void uploadImage_ValidFile_ShouldReturnUrl() throws IOException { //NoSonar
        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("url", "https://cloudinary.com/test-image.jpg");

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), eq(ObjectUtils.emptyMap()))).thenReturn(uploadResult);

        String result = cloudinaryService.uploadImage(multipartFile);

        assertNotNull(result);
        assertEquals("https://cloudinary.com/test-image.jpg", result);
        verify(cloudinary).uploader();
        verify(uploader).upload(multipartFile.getBytes(), ObjectUtils.emptyMap());
    }

    @Test
    void uploadImage_IOException_ShouldThrowException() throws IOException { //NoSonar
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), eq(ObjectUtils.emptyMap())))
                .thenThrow(new IOException("Upload failed"));

        IOException exception = assertThrows(IOException.class, () ->
                cloudinaryService.uploadImage(multipartFile));
        assertEquals("Upload failed", exception.getMessage());
        verify(cloudinary).uploader();
        verify(uploader).upload(multipartFile.getBytes(), ObjectUtils.emptyMap());
    }

    @Test
    void uploadImage_EmptyFile_ShouldThrowIOException() throws IOException { //NoSonar
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), eq(ObjectUtils.emptyMap())))
                .thenThrow(new IOException("Empty file"));

        IOException exception = assertThrows(IOException.class, () ->
                cloudinaryService.uploadImage(emptyFile));
        assertEquals("Empty file", exception.getMessage());
        verify(cloudinary).uploader();
        verify(uploader).upload(emptyFile.getBytes(), ObjectUtils.emptyMap());
    }



    @Test
    void uploadImage_NonImageContentType_ShouldThrowIOException() throws IOException { //NoSonar
        MockMultipartFile nonImageFile = new MockMultipartFile(
                "file",
                "document.txt",
                "text/plain",
                "text content".getBytes()
        );

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), eq(ObjectUtils.emptyMap())))
                .thenThrow(new IOException("Invalid file type: only images are allowed"));

        IOException exception = assertThrows(IOException.class, () ->
                cloudinaryService.uploadImage(nonImageFile));
        assertEquals("Invalid file type: only images are allowed", exception.getMessage());
        verify(cloudinary).uploader();
        verify(uploader).upload(nonImageFile.getBytes(), ObjectUtils.emptyMap());
    }

    @Test
    void uploadImage_LargeFile_ShouldThrowIOException() throws IOException { //NoSonar
        byte[] largeContent = new byte[10 * 1024 * 1024]; // 10MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large-image.jpg",
                "image/jpeg",
                largeContent
        );

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), eq(ObjectUtils.emptyMap())))
                .thenThrow(new IOException("File size exceeds limit"));

        IOException exception = assertThrows(IOException.class, () ->
                cloudinaryService.uploadImage(largeFile));
        assertEquals("File size exceeds limit", exception.getMessage());
        verify(cloudinary).uploader();
        verify(uploader).upload(largeFile.getBytes(), ObjectUtils.emptyMap());
    }

    @Test
    void uploadImage_MissingUrlInResponse_ShouldThrowRuntimeException() throws IOException { //NoSonar
        Map<String, Object> uploadResult = new HashMap<>();
        // No "url" key in the response
        uploadResult.put("other", "value");

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), eq(ObjectUtils.emptyMap()))).thenReturn(uploadResult);

        assertThrows(NullPointerException.class, () -> cloudinaryService.uploadImage(multipartFile));
        verify(cloudinary).uploader();
        verify(uploader).upload(multipartFile.getBytes(), ObjectUtils.emptyMap());
    }

    @Test
    void uploadImage_CloudinaryConfigError_ShouldThrowIOException() throws IOException { //NoSonar
        when(cloudinary.uploader()).thenThrow(new RuntimeException("Cloudinary configuration error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                cloudinaryService.uploadImage(multipartFile));
        assertEquals("Cloudinary configuration error", exception.getMessage());
        verify(cloudinary).uploader();
        verify(uploader, never()).upload(any(), any());
    }
}