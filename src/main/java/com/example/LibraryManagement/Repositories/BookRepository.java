package com.example.LibraryManagement.Repositories;


import com.example.LibraryManagement.Entities.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}