package com.example.LibraryManagement.Controllers

import com.example.LibraryManagement.DTO.AuthRequest
import com.example.LibraryManagement.DTO.AuthResponse
import com.example.LibraryManagement.Services.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

import static org.hamcrest.Matchers.is
import static org.springframework.http.ResponseEntity.status
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@ContextConfiguration(classes = [UserConfig])
@ActiveProfiles("test")
class AuthControllerITSpec extends BaseSpec {

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    UserService userService

    def "register_returnsCreated"() {
        given:
        def request = new AuthRequest("testuser", "testpass")

        1 * userService.register(_) >>  status(201).body("User registered successfully.")

        expect:
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully."))
    }

    def "login_returnsJwtToken"() {
        given:
        def request = new AuthRequest("john_mass_assesment", "password123")
        def response = new AuthResponse("mocked-jwt-token")

        and:
        1 * userService.login(_) >> response

        expect:
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.token', is("mocked-jwt-token")))
    }

    @TestConfiguration
    static class UserConfig {
        @Bean @Primary
        UserService mockService() {
            factory.Mock(UserService)
        }
    }
}