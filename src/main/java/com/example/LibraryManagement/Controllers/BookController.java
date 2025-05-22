package com.example.LibraryManagement.Controllers;

import com.example.LibraryManagement.DTO.BookCreation;
import com.example.LibraryManagement.Services.BookService;
import com.example.LibraryManagement.Entities.Book;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RequiredArgsConstructor
@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping("/{id}")
    public Book getBookById(@PathVariable("id") Long id) {
        return bookService.getBookById(id);
    }

    @PostMapping
    public Book addBook(@Valid @RequestBody BookCreation book) {return bookService.addBook(book);}

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable("id") Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
