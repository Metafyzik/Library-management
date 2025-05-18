package com.example.LibraryManagement.Controllers;

import com.example.LibraryManagement.DTO.AuthRequest;
import com.example.LibraryManagement.DTO.AuthResponse;
import com.example.LibraryManagement.Services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody AuthRequest request) {
        return userService.register(request);
    }
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return userService.login(request);
    }
}
