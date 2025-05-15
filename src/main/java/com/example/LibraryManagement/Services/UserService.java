package com.example.LibraryManagement.Services;

import com.example.LibraryManagement.Config.JwtUtil;
import com.example.LibraryManagement.DTO.AuthRequest;
import com.example.LibraryManagement.DTO.AuthResponse;
import com.example.LibraryManagement.Entities.User;
import com.example.LibraryManagement.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import java.util.Set;

@RequiredArgsConstructor
public class UserService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;

    public ResponseEntity<String> register(@RequestBody AuthRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(Role.MEMBER)); // Default role
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully.");
    }

    public AuthResponse login(@RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());

        String token = jwtUtil.generateToken(userDetails);
        return new AuthResponse(token);
    }

    public User findByUsername (String userName){
        return userRepository.findByUsername(userName).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found with username: " + userName));
    }
}
