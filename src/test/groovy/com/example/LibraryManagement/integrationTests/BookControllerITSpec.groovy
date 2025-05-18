package com.example.LibraryManagement.integrationTests

import com.example.LibraryManagement.Entities.Book
import com.example.LibraryManagement.Repositories.BookRepository
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonOutput
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.hamcrest.Matchers.hasSize
import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookControllerITSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @Autowired
    BookRepository bookRepository

    def cleanup() {
        bookRepository.deleteAll()
    }

    Book createBook(String title, boolean available = true) {
        def book = new Book()
        book.title = title
        book.author = "Author"
        book.available = available
        bookRepository.save(book)
    }

    //POST /books; METHOD = addBook

    @WithMockUser(username = "testuser", roles = ["MEMBER"])
    def "creates a new book and returns it and is in db"() {
        given:
        def title = "book title"
        def author = "an author"
        def bookJson = JsonOutput.toJson([title:title, author: author])

        expect: "created book is returned to client"
        def response = mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookJson)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.title', is(title)))
                .andExpect(jsonPath('$.available', is(true)))
                .andReturn() // capture the result

        "get book id from the response"
        def objectMapper = new ObjectMapper()
        def responseBody = response.response.contentAsString
        def responseJson = objectMapper.readTree(responseBody)
        def id = responseJson.get("id").asLong()

        and: "book is saved to database"
        def createdBook = bookRepository.findById(id)
        createdBook.isPresent()
    }

    @WithMockUser(username = "testuser", roles = ["MEMBER"])
    def "returns error when author is missing"() {
        given:

        def bookJson = JsonOutput.toJson([title: "1984"])

        expect:
        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookJson)
        )
                .andExpect(status().isBadRequest())
    }

    @WithMockUser(username = "testuser", roles = ["MEMBER"])
    def "returns error when title is missing"() {
        given:

        def bookJson = JsonOutput.toJson([author: "Anthony Burgess"])

        expect:
        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookJson)
        )
                .andExpect(status().isBadRequest())
    }

    //GET /books; METHOD: getAllBooks

    @WithMockUser(username = "testuser", roles = ["MEMBER"])
    def "return all books"() {
        given:

        createBook("title 1", true)
        createBook("title 2", false)

        expect:
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$', hasSize(2)))
                .andExpect(jsonPath('$[0].title', is("title 1")))
                .andExpect(jsonPath('$[0].available', is(true)))
                .andExpect(jsonPath('$[1].title', is("title 2")))
                .andExpect(jsonPath('$[1].available', is(false)))
    }

    //GET /books/{id}; METHOD: getBookById

    @WithMockUser(username = "testuser", roles = ["MEMBER"])
    def "return a book by id"() {
        given:
        def book = createBook("1985")

        expect:
        mockMvc.perform(get("/books/${book.id}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('id', is(book.id.intValue())))
    }


    @WithMockUser(username = "testuser", roles = ["MEMBER"])
    def "return not foud when there is no but with such id to return"() {
        def nonExistingId = 999
        when:
        def mvcResult = mockMvc.perform(
                get("/books/${nonExistingId}")
        ).andReturn()

        then:
        mvcResult.response.status == 404
        mvcResult.response.errorMessage ==  "Book with ID " + nonExistingId + " not found"
    }

    //DEL /books/{id}; METHOD: deleteBook

    @WithMockUser(username = "testuser", roles = ["MEMBER"])
    def "return 204 when book is properly deleted"() {
        def book = createBook("1985")

        when:
        def mvcResult = mockMvc.perform(
                delete("/books/${book.id}")
        ).andReturn()

        then:
        mvcResult.response.status == 204

    }

    @WithMockUser(username = "testuser", roles = ["MEMBER"])
    def "return not foud when there is no but with such id to delete"() {
        def nonExistingId = 999
        when:
        def mvcResult = mockMvc.perform(
                delete("/books/${nonExistingId}")
        ).andReturn()

        then:
        mvcResult.response.status == 404
        mvcResult.response.errorMessage ==  "Book with ID " + nonExistingId + " not found"
    }
}
