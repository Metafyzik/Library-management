package com.example.LibraryManagement.Controllers

import com.example.LibraryManagement.Entities.Book
import com.example.LibraryManagement.Services.BookService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration

import static org.hamcrest.Matchers.hasSize
import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [BookConfig])
class BookControllerITSpec extends BaseSpec {

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    BookService bookService

    @WithMockUser(username = "user", roles = ["MEMBER"])
    def "GET /books returns list of books"() {
        given:
        bookService.getAllBooks() >> [
                new Book(1L, "1984", "George Orwell", true),
                new Book(2L, "Brave New World", "Aldous Huxley", false)
        ]

        expect:
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$', hasSize(2)))
                .andExpect(jsonPath('$[0].title', is("1984")))
    }

    @WithMockUser(username = "user", roles = ["MEMBER"])
    def "POST /books creates a new book"() {
        given:
        def input = new Book(null, "Spock Testing", "Tester", true)
        def saved = new Book(101L, "Spock Testing", "Tester", true)
        bookService.saveBook(_) >> saved

        expect:
        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.id', is(101)))
                .andExpect(jsonPath('$.title', is("Spock Testing")))
    }

    @TestConfiguration
    static class BookConfig {
        @Bean @Primary
        BookService mockService() {
            factory.Mock(BookService)
        }
    }
}