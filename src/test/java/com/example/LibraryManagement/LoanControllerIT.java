package com.example.LibraryManagement;

import com.example.LibraryManagement.Entities.Book;
import com.example.LibraryManagement.Entities.Loan;
import com.example.LibraryManagement.Entities.User;
import com.example.LibraryManagement.Services.LoanService;
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
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//TODO individual imports

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(LoanControllerIT.MockedServiceConfig.class)
class LoanControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoanService loanService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class MockedServiceConfig {
        @Bean
        @Primary
        public LoanService mockLoanService() {
            return Mockito.mock(LoanService.class);
        }
    }

    @Test
    @WithMockUser(username = "user", roles = {"MEMBER"}) // to bypass the JWT token auth
    void borrowBook_createsLoan() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        Book book = new Book(1L, "Book Title", "Author", false);

        Loan loan = new Loan();
        loan.setId(10L);
        loan.setBook(book);
        loan.setUser(user);
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(14)); //TODO magic number
        loan.setReturned(false);

        Mockito.when(loanService.borrowBook(Mockito.eq(1L), Mockito.any(User.class))).thenReturn(loan);

        mockMvc.perform(post("/loans/1/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.book.title", is("Book Title")))
                .andExpect(jsonPath("$.user.username", is("testuser")));
    }

    @Test
    @WithMockUser(username = "user", roles = {"MEMBER"}) // to bypass the JWT token auth
    void getUserLoans_returnsListOfLoans() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        Book book = new Book(1L, "Book Title", "Author", false);

        Loan loan = new Loan();
        loan.setId(11L);
        loan.setBook(book);
        loan.setUser(user);
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(14));
        loan.setReturned(false);

        List<Loan> loans = List.of(loan);

        Mockito.when(loanService.getLoansByUser(1L)).thenReturn(loans);

        mockMvc.perform(get("/loans/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(11)))
                .andExpect(jsonPath("$[0].book.title", is("Book Title")));
    }
}
