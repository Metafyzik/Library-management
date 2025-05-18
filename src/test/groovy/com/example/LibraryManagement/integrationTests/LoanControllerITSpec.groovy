package com.example.LibraryManagement

import com.example.LibraryManagement.Entities.Book
import com.example.LibraryManagement.Entities.Loan
import com.example.LibraryManagement.Entities.User
import com.example.LibraryManagement.Repositories.BookRepository
import com.example.LibraryManagement.Repositories.LoanRepository
import com.example.LibraryManagement.Repositories.UserRepository
import com.example.LibraryManagement.Services.Role
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import java.time.LocalDate

import static com.example.LibraryManagement.Services.LoanService.DEFAULT_LOAN_DURATION_DAYS
import static org.hamcrest.Matchers.hasSize
import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoanControllerITSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @Autowired
    UserRepository userRepository

    @Autowired
    BookRepository bookRepository

    @Autowired
    LoanRepository loanRepository


    def cleanup() {
        loanRepository.deleteAll()
        bookRepository.deleteAll()
        userRepository.deleteAll()
    }

    User createUser(String username) {
        def user = new User()
        user.username = username
        user.password = "password"
        user.roles = Set.of(Role.MEMBER)
        userRepository.save(user)
    }

    Book createBook(String title, boolean available = true) {
        def book = new Book()
        book.title = title
        book.author = "Author"
        book.available = available
        bookRepository.save(book)
    }

    Loan createLoan(User user, Book book, boolean returned = false ) {
        def loan = new Loan()
        loan.book = book
        loan.user = user
        loan.loanDate = LocalDate.now()
        loan.dueDate = LocalDate.now().plusDays(DEFAULT_LOAN_DURATION_DAYS)
        loan.returned = returned
        loanRepository.save(loan)
    }

    // POST /loans/{bookId}/borrow; METHOD: borrowBook

    @WithMockUser(username = "testuser", roles = ["MEMBER"])
    def "borrowBook returns a new loan when book is available"() {
        given:
        def user = createUser("testuser")
        def book = createBook("Book Title")

        when:
        def response = mockMvc.perform(
                post("/loans/${book.id}/${user.username}/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
        )

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.book.title', is("Book Title")))
                .andExpect(jsonPath('$.user.username', is("testuser")))
                .andExpect(jsonPath('$.returned', is(false)))

        and: "Book is now unavailable"
        def updatedBook = bookRepository.findById(book.id).get()
        !updatedBook.available
    }

    @WithMockUser(username = "testuser", roles = ["MEMBER"])
    def "returns a conflict when a book is already borrowed"() {
        given:
        def user = createUser("testuser")
        def book = createBook("Book Title", false)

        when:
        def mvcResult = mockMvc.perform(
                post("/loans/${book.id}/${user.username}/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andReturn()

        then:
        mvcResult.response.status == 409
        mvcResult.response.errorMessage == "Book with ID ${book.id} is already borrowed"
    }

    @WithMockUser(username = "testuser", roles = ["MEMBER"])
    def "returns not found when user doesn't exist"() {
        given:
        def book = createBook("Book Title")

        def nonExistentUserName= "IdontExist"

        when:
        def mvcResult = mockMvc.perform(
                post("/loans/${book.id}/${nonExistentUserName}/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andReturn()

        then:
        mvcResult.response.status == 404
        mvcResult.response.errorMessage == "User not found with username: ${nonExistentUserName}"
    }

    // PUT /loans/{loanId}/return; METHOD: returnBook

    @WithMockUser(username = "testuser", roles = ["MEMBER"])
    def "returns a loan and marks book as available when loan exist and is not returned yet"() {
        given:
        def user = createUser("testuser")
        def book = createBook("Book Title", false)
        def loan = createLoan(user, book)

        expect: "updated loan is returned to client"
        mockMvc.perform(put("/loans/${loan.id}/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.id', is(loan.id.intValue())))
                .andExpect(jsonPath('book.title', is("Book Title")))
                .andExpect(jsonPath('$.user.username', is("testuser")))
                .andExpect(jsonPath('$.book.available', is(true)))

        and: "updates are saved to database"
        def updatedLoan = loanRepository.findById(loan.id).get()
        updatedLoan.returned
        updatedLoan.book.available
    }

    @WithMockUser(username = "testuser", roles = ["MEMBER"])
    def "returnBook returns 404 if loan does not exist"() {
        expect:
        mockMvc.perform(put("/loans/999/return"))
                .andExpect(status().isNotFound())

    }

    @WithMockUser(username = "testuser", roles = ["MEMBER"])
    def "returnBook returns 400 if loan is already returned"() {
        given:
        def user = createUser("testuser")
        def book = createBook("Book Title")
        def loan = createLoan(user, book,true)

        expect: "updated loan is returned to client"
        mockMvc.perform(put("/loans/${loan.id}/return"))
                .andExpect(status().isBadRequest())
    }

    // GET /loans/user/{userid}; METHOD: getUserLoans

    @WithMockUser(username = "testuser", roles = ["MEMBER"])
    def "returns all books borrowed books by the user"() {
        given:
        def user = createUser("testuser")
        def book1 = createBook("title 1", false)
        def book2 = createBook("title 2", false)

        createLoan(user, book1)
        createLoan(user, book2)

        expect:
        mockMvc.perform(get("/loans/user/${user.id}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$', hasSize(2)))
                .andExpect(jsonPath('$[0].book.title', is("title 1")))
                .andExpect(jsonPath('$[0].book.available', is(false)))
                .andExpect(jsonPath('$[1].book.title', is("title 2")))
                .andExpect(jsonPath('$[1].book.available', is(false)))
    }

    @WithMockUser(username = "testuser", roles = ["MEMBER"])
    def "returns empty List when use does not exist"() {
        expect:
        mockMvc.perform(get("/loans/user/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$', hasSize(0)))
    }

    @WithMockUser(username = "testuser", roles = ["MEMBER"])
    def "returns empty List when user has no books borrowed"() {
        given:
        def user = createUser("testuser")

        expect: "updated loan is returned to client"
        mockMvc.perform(get("/loans/user/${user.id}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$', hasSize(0)))
    }

}
