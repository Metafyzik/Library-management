package com.example.LibraryManagement.Controllers;

import com.example.LibraryManagement.Repositories.UserRepository;
import com.example.LibraryManagement.Services.Role;
import com.example.LibraryManagement.DTO.AuthRequest;
import com.example.LibraryManagement.DTO.AuthResponse;
import com.example.LibraryManagement.Services.CustomUserDetailsService;
import com.example.LibraryManagement.Config.JwtUtil;
import com.example.LibraryManagement.Entities.User;
import com.example.LibraryManagement.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    public AuthController( UserService userService) {
        this.userService = userService;
    }
    @PostMapping("/register")
    public String register(@RequestBody AuthRequest request) {
        return userService.register(request);
    }
    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        return userService.login(request);
    }
}
