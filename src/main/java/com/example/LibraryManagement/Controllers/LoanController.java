package com.example.LibraryManagement.Controllers;

import com.example.LibraryManagement.Entities.Loan;
import com.example.LibraryManagement.Entities.User;
import com.example.LibraryManagement.Services.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/loans")
public class LoanController {
    private final LoanService loanService;

    @PostMapping("/{bookId}/{username}/borrow")
    public Loan borrowBook(@PathVariable("bookId") Long bookId, @PathVariable("username") String userName) {
        return loanService.borrowBook(bookId, userName);
    }

    @PutMapping("/{loanId}/return")
    public ResponseEntity<Loan> returnBook(@PathVariable("loanId") Long loanId) {
        Loan updatedLoan = loanService.returnBook(loanId);
        return ResponseEntity.ok(updatedLoan);
    }

    @GetMapping("/user/{userId}")
    public List<Loan> getUserLoans(@PathVariable Long userId) {
        return loanService.getLoansByUser(userId);
    }
}
