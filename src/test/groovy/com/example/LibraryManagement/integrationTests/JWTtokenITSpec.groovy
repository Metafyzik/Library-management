package com.example.LibraryManagement.integrationTests

import com.example.LibraryManagement.DTO.AuthRequest
import com.example.LibraryManagement.Repositories.BookRepository
import com.example.LibraryManagement.Repositories.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JWTtokenITSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    UserRepository userRepository

    @Autowired
    BookRepository bookRepository

    def cleanup() {
        userRepository.deleteAll()
        bookRepository.deleteAll()
    }

    //change case of the first letter in payload part of the token
    def tamperTokenPayload(String input) {
        int dotIndex = input.indexOf('.')

        char targetChar = input.charAt(dotIndex + 1)
        char toggledChar = targetChar.isUpperCase() ? targetChar.toLowerCase() : targetChar.toUpperCase()

        // Build new string
        return input[0..dotIndex] + toggledChar + input[(dotIndex + 2)..-1]
    }

    private String registerAndLogin(String username) {
        def request = new AuthRequest(username, "pass")

        // Register user
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())

        // Login user
        def result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn()

        // Extract token
        def responseJson = objectMapper.readTree(result.response.contentAsString)
        return responseJson.get("token").asText()
    }

    def "accessing /books endpoint with valid token returns ok"() {
        given:
        def jwt = registerAndLogin("Jack")

        expect:
        mockMvc.perform(get("/books")
                .header("Authorization", "Bearer $jwt")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
    }

    def "accessing /books endpoint with tampered token returns error"() {
        given:
        def jwt = registerAndLogin("Jack")
        def tampered = tamperTokenPayload(jwt)

        expect:
        mockMvc.perform(post("/books")
                .header("Authorization", "Bearer $tampered")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath('$.error', is("Invalid or expired token")))
    }
}
