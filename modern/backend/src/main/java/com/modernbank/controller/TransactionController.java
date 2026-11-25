package com.modernbank.controller;

import com.modernbank.dto.*;
import com.modernbank.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.deposit(request));
    }

    @PostMapping("/withdrawal")
    public ResponseEntity<TransactionResponse> withdrawal(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.withdrawal(request));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(transactionService.transfer(request));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<List<TransactionHistoryItem>> getHistory(
            @PathVariable String accountNumber,
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(transactionService.getHistory(accountNumber, type));
    }
}
