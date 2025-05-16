package com.example.LibraryManagement.Services

import com.example.LibraryManagement.Entities.Book
import com.example.LibraryManagement.Entities.Loan
import com.example.LibraryManagement.Entities.User
import com.example.LibraryManagement.Repositories.LoanRepository
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification

import java.time.LocalDate

class LoanServiceSpec extends Specification {

    def loanRepository = Mock(LoanRepository)
    def bookService = Mock(BookService)
    def userService = Mock(UserService)

    def loanService = new LoanService(loanRepository, bookService, userService)

    def "borrowBook should create and save a loan when book is available"() {
        given:
        def book = new Book(id: 1L, available: true)
        def user = new User(username: "johndoe")
        def storedUser = new User(id: 2L, username: "johndoe")
        def savedLoan = new Loan(id: 10L)

        bookService.getBookById(1L) >> book
        userService.findByUsername("johndoe") >> storedUser
        loanRepository.save(_ as Loan) >> { Loan loan ->
            assert loan.book == book
            assert loan.user == storedUser
            assert loan.loanDate == LocalDate.now()
            assert loan.dueDate == LocalDate.now().plusDays(14)
            assert !loan.returned
            return savedLoan
        }

        when:
        def result = loanService.borrowBook(1L, user)

        then:
        1 * bookService.saveBook(book)
        result == savedLoan
    }

    def "borrowBook should throw exception if book is not available"() {
        given:
        def book = new Book(id: 1L, available: false)
        def user = new User(username: "johndoe")

        bookService.getBookById(1L) >> book

        when:
        loanService.borrowBook(1L, user)

        then:
        def ex = thrown(ResponseStatusException)
        ex.statusCode == HttpStatus.CONFLICT
        ex.reason.contains("already borrowed")
    }

    def "returnBook should mark loan as returned and update book availability"() {
        given:
        def book = new Book(id: 1L, available: false)
        def loan = new Loan(id: 1L, book: book, returned: false)

        loanRepository.findById(1L) >> Optional.of(loan)

        when:
        loanService.returnBook(1L)

        then:
        loan.returned
        loan.book.available
        1 * bookService.saveBook(book)
        1 * loanRepository.save(loan)
    }

    def "returnBook should throw exception if loan is not found"() {
        given:
        loanRepository.findById(999L) >> Optional.empty()

        when:
        loanService.returnBook(999L)

        then:
        def ex = thrown(ResponseStatusException)
        ex.statusCode == HttpStatus.NOT_FOUND
        ex.reason.contains("not found")
    }

    def "getLoansByUser should delegate to loanRepository"() {
        given:
        def userId = 42L
        def loans = [new Loan(), new Loan()]
        loanRepository.findByUserId(userId) >> loans

        expect:
        loanService.getLoansByUser(userId) == loans
    }
}
