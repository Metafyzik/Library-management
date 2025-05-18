package com.example.LibraryManagement.integrationTests

import com.example.LibraryManagement.DTO.AuthRequest
import com.example.LibraryManagement.Entities.User
import com.example.LibraryManagement.Repositories.UserRepository
import com.example.LibraryManagement.Services.Role
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonOutput
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*


@ContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerITSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    UserRepository userRepository

    @Autowired
    PasswordEncoder passwordEncoder

    final String DECODED_PASSWORD = "pass"

    def cleanup() {
        userRepository.deleteAll()
    }

    User createUser(String username) {
        def user = new User()
        user.username = username
        user.password = passwordEncoder.encode(DECODED_PASSWORD)
        user.roles = Set.of(Role.MEMBER)
        userRepository.save(user)
    }

    //POST /auth/register; METHOD: register

    def "returns created when the user registered successfully."() {
        given:
        def request = new AuthRequest("newuser", "password123")
        expect:
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully."))
    }

    def "returns conflict when username is already taken"() {
        given:
        def duplicateName = "existinguser"
        createUser(duplicateName)

        def request = new AuthRequest(duplicateName, "password123")

        when:
        def result = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andReturn()

        then:
        result.response.status == 409
        result.response.errorMessage == "Username is already taken"
    }


    def "returns bad request when username is missing from the registration request"() {
        given:

        def authJson = JsonOutput.toJson([password: "pass1313215",])

        expect:
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authJson)
        )
                .andExpect(status().isBadRequest())
    }

    def "returns bad request when password is missing from the registration request"() {
        given:

        def authJson = JsonOutput.toJson([author: "George Orwell"])

        expect:
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authJson)
        )
                .andExpect(status().isBadRequest())
    }

    //POST /auth/login; METHOD: login

    def "should login successfully and return JWT token"() {
        given:
        def user = createUser("userName")

        def request = new AuthRequest(user.username, DECODED_PASSWORD)

        when:
        def result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        then:
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.token').exists())
    }

    def "returns bad request when username is missing from the login request"() {
        given:

        def authJson = JsonOutput.toJson([password: "pass1313215",])

        expect:
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authJson)
        )
                .andExpect(status().isBadRequest())
    }

    def "returns bad request when password is missing from the login request"() {
        given:

        def authJson = JsonOutput.toJson([author: "George Orwell"])

        expect:
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authJson)
        )
                .andExpect(status().isBadRequest())
    }

}
