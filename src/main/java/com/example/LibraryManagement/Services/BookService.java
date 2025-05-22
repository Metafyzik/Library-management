package com.example.LibraryManagement.Services;

import com.example.LibraryManagement.DTO.BookCreation;
import com.example.LibraryManagement.Entities.Book;
import com.example.LibraryManagement.Repositories.BookRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(Long id) {

        return bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Book with ID " + id + " not found"
                ));
    }

    public Book addBook(BookCreation bookCreation) {
        Book createdBook = new Book(bookCreation.title(),bookCreation.author());

        return bookRepository.save(createdBook);
    }

    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Book with ID " + id + " not found"
            );
        }
        bookRepository.deleteById(id);
    }
}
