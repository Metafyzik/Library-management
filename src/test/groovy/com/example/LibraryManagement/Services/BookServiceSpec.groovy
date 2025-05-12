package com.example.LibraryManagement.Services

import com.example.LibraryManagement.Entities.Book
import com.example.LibraryManagement.Repositories.BookRepository
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification
import spock.lang.Subject

class BookServiceSpec extends Specification {

    BookRepository bookRepository = Mock()

    @Subject
    BookService bookService = new BookService(bookRepository)

    def "getAllBooks returns list of books"() {
        given:
        def books = [
                new Book(1L, "1984", "George Orwell", true),
                new Book(2L, "Brave New World", "Aldous Huxley", true)
        ]
        bookRepository.findAll() >> books

        when:
        def result = bookService.getAllBooks()

        then:
        result.size() == 2
        result[0].title == "1984"
        result[1].author == "Aldous Huxley"
    }

    def "getBookById returns book when found"() {
        given:
        def book = new Book(1L, "Dune", "Frank Herbert", true)
        bookRepository.findById(1L) >> Optional.of(book)

        when:
        def result = bookService.getBookById(1L)

        then:
        result.id == 1L
        result.title == "Dune"
        result.author == "Frank Herbert"
        result.isAvailable
    }

    def "getBookById throws exception when not found"() {
        given:
        long id = 1l
        bookRepository.findById(id) >> Optional.empty()

        when:
        bookService.getBookById(id)

        then:
        def ex = thrown(ResponseStatusException)
        ex.message == "404 NOT_FOUND \"Book with ID " +id +" not found\""
    }

    def "addBook saves and returns book"() {
        given:
        def bookToAdd = new Book(null, "Clean Code", "Robert C. Martin", true)
        def savedBook = new Book(10L, "Clean Code", "Robert C. Martin", true)
        bookRepository.save(bookToAdd) >> savedBook

        when:
        def result = bookService.saveBook(bookToAdd)

        then:
        result.id == 10L
        result.title == "Clean Code"
        result.author == "Robert C. Martin"
        result.isAvailable
    }

    def "deleteBook calls repository deleteById"() {
        when:
        bookService.deleteBook(99L)

        then:
        1 * bookRepository.deleteById(99L)
    }

}
