package com.example.LibraryManagement.DTO;

import jakarta.validation.constraints.NotBlank;

import static com.example.LibraryManagement.Common.ValidationMessages.AUTHOR_NOT_BLANK;
import static com.example.LibraryManagement.Common.ValidationMessages.TITLE_NOT_BLANK;

public record BookCreation(
        @NotBlank(message = AUTHOR_NOT_BLANK) String author,
        @NotBlank(message = TITLE_NOT_BLANK) String title
) {}