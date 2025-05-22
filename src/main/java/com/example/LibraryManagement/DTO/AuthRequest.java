package com.example.LibraryManagement.DTO;

import jakarta.validation.constraints.NotBlank;

import static com.example.LibraryManagement.Common.ValidationMessages.PASSWORD_NOT_BLANK;
import static com.example.LibraryManagement.Common.ValidationMessages.USERNAME_NOT_BLANK;


public record AuthRequest(
        @NotBlank(message = USERNAME_NOT_BLANK) String username,
        @NotBlank(message = PASSWORD_NOT_BLANK) String password
) {}