package com.example.LibraryManagement.Controllers;

import com.example.LibraryManagement.Services.LoanService;
import com.example.LibraryManagement.Tables.Loan;
import com.example.LibraryManagement.Tables.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loans")
public class LoanController {
    @Autowired
    private LoanService loanService;

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
