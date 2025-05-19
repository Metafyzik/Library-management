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

import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

import static com.example.LibraryManagement.Common.ValidationMessages.PASSWORD_NOT_BLANK;
import static com.example.LibraryManagement.Common.ValidationMessages.USERNAME_NOT_BLANK;


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


    def "returns bad request and json when username is missing from the registration request"() {
        given:

        def authJson = JsonOutput.toJson([password: "pass1313215",])

        expect:
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authJson)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.username', is(USERNAME_NOT_BLANK)))
    }

    def "returns bad request and json when password is missing from the registration request"() {
        given:

        def authJson = JsonOutput.toJson([username: "George Orwell"])

        expect:
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authJson)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.password', is(PASSWORD_NOT_BLANK)))
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

    def "returns bad request and json when username is missing from the login request"() {
        given:

        def authJson = JsonOutput.toJson([password: "pass1313215",])

        expect:
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authJson)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.username', is(USERNAME_NOT_BLANK)))
    }

    def "returns bad request and json when password is missing from the login request"() {
        given:

        def authJson = JsonOutput.toJson([username: "George Orwell"])

        expect:
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authJson)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.password', is(PASSWORD_NOT_BLANK)))
    }

    //Combine registration a logging check

    def "register a new user and login and return JWT token"() {
        given:

        def username ="Jack"
        def request = new AuthRequest(username, DECODED_PASSWORD)

        when: "register a new user"
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully."))

        then: "login a user"
        def result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        and: "and receive a token"
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.token').exists())
    }

    def "register a new user and login with password and receive an error"() {
        given:
        def username ="Jack"

        def request = new AuthRequest(username, DECODED_PASSWORD)
        def wrongRequest = new AuthRequest(username, "wrongPassword")

        when: "successfully register user"
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully."))

        then: "login a user"
        def result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongRequest)))
                .andReturn()

        and: "and receive an error"
        result.response.status == 403
        result.response.errorMessage == "Access Denied"

    }
}
