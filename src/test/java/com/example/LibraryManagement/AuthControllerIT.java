package com.example.LibraryManagement;

import com.example.LibraryManagement.DTO.AuthRequest;
import com.example.LibraryManagement.DTO.AuthResponse;
import com.example.LibraryManagement.Services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(AuthControllerIT.MockedServiceConfig.class)
@ActiveProfiles("test")
class AuthControllerIT {

    private final MockMvc mockMvc;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @TestConfiguration
    static class MockedServiceConfig {
        @Bean
        @Primary
        public UserService mockUserService() {
            return Mockito.mock(UserService.class);
        }
    }

    @Test
    void register_returnsCreated() throws Exception {
        AuthRequest request = new AuthRequest("testuser", "testpass");

        Mockito.when(userService.register(Mockito.any(AuthRequest.class)))
                .thenReturn(ResponseEntity.status(201).body("User registered successfully."));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully."));
    }

    @Test
    void login_returnsJwtToken() throws Exception {
        AuthRequest request = new AuthRequest("john_mass_assesment", "password123");
        AuthResponse response = new AuthResponse("mocked-jwt-token");

        Mockito.when(userService.login(Mockito.any()))
                .thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("mocked-jwt-token")));
    }
}
