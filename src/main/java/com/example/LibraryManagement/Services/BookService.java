package com.example.LibraryManagement.Services;

import com.example.LibraryManagement.Entities.Book;
import com.example.LibraryManagement.Repositories.BookRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

public class BookService {
    private final BookRepository bookRepository;
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(Long id) {

        return bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Book with ID " + id + " not found"
                ));
    }

    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    public Book addBook(Book book) {
        return bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }
}
