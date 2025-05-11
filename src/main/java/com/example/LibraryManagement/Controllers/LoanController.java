package com.example.LibraryManagement.Controllers;

import com.example.LibraryManagement.Entities.Loan;
import com.example.LibraryManagement.Entities.User;
import com.example.LibraryManagement.Services.LoanService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loans")
public class LoanController {
    private final LoanService loanService;
    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }
    @PostMapping("/{bookId}/borrow")
    public Loan borrowBook(@PathVariable Long bookId, @RequestBody User user) {
        return loanService.borrowBook(bookId, user);
    }

    @PutMapping("/{loanId}/return")
    public void returnBook(@PathVariable Long loanId) {
        loanService.returnBook(loanId);
    }

    @GetMapping("/user/{userId}")
    public List<Loan> getUserLoans(@PathVariable Long userId) {
        return loanService.getLoansByUser(userId);
    }
}
