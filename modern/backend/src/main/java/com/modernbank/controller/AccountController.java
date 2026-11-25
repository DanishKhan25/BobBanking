package com.modernbank.controller;

import com.modernbank.dto.AccountDetailsResponse;
import com.modernbank.dto.DashboardSummary;
import com.modernbank.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<AccountDetailsResponse> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.findByAccountNumber(accountNumber));
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<DashboardSummary> getDashboardSummary() {
        return ResponseEntity.ok(accountService.getDashboardSummary());
    }
}
