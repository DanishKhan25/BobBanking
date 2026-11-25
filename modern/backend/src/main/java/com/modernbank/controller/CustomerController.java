package com.modernbank.controller;

import com.modernbank.dto.CustomerResponse;
import com.modernbank.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.findById(id));
    }

    @GetMapping("/by-account/{accountNumber}")
    public ResponseEntity<CustomerResponse> getByAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(customerService.findByAccountNumber(accountNumber));
    }
}
