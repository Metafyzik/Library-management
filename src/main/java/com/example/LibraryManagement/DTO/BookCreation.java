package com.example.LibraryManagement.DTO;

import jakarta.validation.constraints.NotBlank;

public record BookCreation(
        @NotBlank(message = "Author must not be blank") String author,
        @NotBlank(message = "Title must not be blank") String title
) {}