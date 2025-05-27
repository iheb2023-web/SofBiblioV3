package com.sofrecom.backend.dtos;

import com.sofrecom.backend.enums.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    private String firstname;
    private String lastname;
    private String email;
    private String image;
    private String job;
    private LocalDate birthday;
    private Long number;
    @Enumerated(EnumType.STRING)
    private Role role;
}