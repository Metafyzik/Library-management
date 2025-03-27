package com.example.LibraryManagement.Services;

import com.example.LibraryManagement.Repositories.BookRepository;
import com.example.LibraryManagement.Repositories.LoanRepository;
import com.example.LibraryManagement.Entities.Book;
import com.example.LibraryManagement.Entities.Loan;
import com.example.LibraryManagement.Entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class LoanService {
    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BookRepository bookRepository; //TODO change it to constructor injection

    //TODO make special exceptions for not existing loan and book
    public Loan borrowBook(Long bookId, User user) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new RuntimeException("Book not found"));
        if (!book.isAvailable()) {
            throw new RuntimeException("Book is already borrowed");
        }

        book.setAvailable(false);
        bookRepository.save(book);

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setUser(user);
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(14));
        loan.setReturned(false);

        return loanRepository.save(loan);
    }

    public void returnBook(Long loanId) {
        Loan loan = loanRepository.findById(loanId).orElseThrow(() -> new RuntimeException("Loan not found"));
        loan.setReturned(true);
        loan.getBook().setAvailable(true);
        bookRepository.save(loan.getBook());
        loanRepository.save(loan);
    }

    public List<Loan> getLoansByUser(Long userId) {
        return loanRepository.findByUserId(userId);
    }
}
