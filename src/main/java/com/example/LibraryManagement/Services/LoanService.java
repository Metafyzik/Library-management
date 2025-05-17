package com.example.LibraryManagement.Services;

import com.example.LibraryManagement.Entities.Book;
import com.example.LibraryManagement.Entities.Loan;
import com.example.LibraryManagement.Entities.User;
import com.example.LibraryManagement.Repositories.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class LoanService {
    public static final int DEFAULT_LOAN_DURATION_DAYS = 14;

    private final LoanRepository loanRepository;
    private final BookService bookService;
    private final UserService userService;

    @Transactional
    public Loan borrowBook(Long bookId, String userName) {
        Book book = bookService.getBookById(bookId);

        if (!book.isAvailable()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Book with ID " + bookId + " is already borrowed"
            );
        }

        User borrowingUser = userService.findByUsername(userName);

        book.setAvailable(false);
        bookService.saveBook(book);

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setUser(borrowingUser);
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(DEFAULT_LOAN_DURATION_DAYS));
        loan.setReturned(false);

        return loanRepository.save(loan);
    }

    @Transactional
    public Loan returnBook(Long loanId) {
        Loan loan = loanRepository.findById(loanId).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Loan with ID " + loanId + " not found"
        ));

        if (loan.isReturned()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Loan with ID " + loanId + " has already been returned"
            );
        }

        loan.setReturned(true);
        loan.getBook().setAvailable(true);
        bookService.saveBook(loan.getBook());
        return loanRepository.save(loan);
    }

    public List<Loan> getLoansByUser(Long userId) {
        return loanRepository.findByUserId(userId);
    }
}
