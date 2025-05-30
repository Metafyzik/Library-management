package com.example.LibraryManagement.Services;

import com.example.LibraryManagement.Entities.User;
import com.example.LibraryManagement.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        //When user is not found it does not throw error, but returns 403 forbidden

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(),
                user.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList()
        );
    }
}
