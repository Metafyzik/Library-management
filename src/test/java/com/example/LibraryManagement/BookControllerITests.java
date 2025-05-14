package com.example.LibraryManagement;


import com.example.LibraryManagement.Entities.Book;
import com.example.LibraryManagement.Services.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(BookControllerITests.MockedServiceConfig.class)
@ActiveProfiles("test")
class BookControllerITests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private BookService bookService;
    @Autowired
    private ObjectMapper objectMapper;


    @TestConfiguration
    static class MockedServiceConfig {
        @Bean
        @Primary
        public BookService mockBookService() {
            return Mockito.mock(BookService.class);
        }
    }

    @Test
    @WithMockUser(username = "user", roles = {"MEMBER"}) // to bypass the JWT token auth
    void getBooks_returnsListOfBooks() throws Exception {
        List<Book> books = List.of(
                new Book(1L, "1984", "George Orwell", true),
                new Book(2L, "Brave New World", "Aldous Huxley", false)
        );

        Mockito.when(bookService.getAllBooks()).thenReturn(books);

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("1984")));
    }


    @Test
    @WithMockUser(username = "user", roles = {"MEMBER"})
    void postBooks_createsNewBook() throws Exception {
        Book input = new Book(null, "Spock Testing", "Tester", true);
        Book saved = new Book(101L, "Spock Testing", "Tester", true);

        Mockito.when(bookService.saveBook(any(Book.class))).thenReturn(saved);

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Spock Testing")))
                .andExpect(jsonPath("$.title", is("Spock Testing")));
    }
}