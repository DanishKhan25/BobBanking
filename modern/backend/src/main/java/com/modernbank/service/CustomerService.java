package com.modernbank.service;

import com.modernbank.domain.Account;
import com.modernbank.domain.Customer;
import com.modernbank.dto.CustomerResponse;
import com.modernbank.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + customerId));
        return toResponse(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse findByAccountNumber(String accountNumber) {
        Customer customer = customerRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("No customer found for account: " + accountNumber));
        return toResponse(customer);
    }

    private CustomerResponse toResponse(Customer customer) {
        CustomerResponse resp = new CustomerResponse();
        resp.setCustomerId(customer.getCustomerId());
        resp.setCustomerName(customer.getCustomerName());
        resp.setEmail(customer.getEmail() != null ? customer.getEmail() : "-");
        resp.setPhone(customer.getPhone() != null ? customer.getPhone() : "-");
        resp.setAddress(customer.getAddress() != null ? customer.getAddress() : "-");
        resp.setCreatedDate(customer.getCreatedDate());

        List<CustomerResponse.AccountSummary> summaries = customer.getAccounts().stream()
                .map(a -> {
                    CustomerResponse.AccountSummary s = new CustomerResponse.AccountSummary();
                    s.setAccountNumber(a.getAccountNumber());
                    s.setAccountType(a.getAccountType().name());
                    s.setStatus(a.getStatus().name());
                    s.setBalance(a.getBalance());
                    return s;
                })
                .collect(Collectors.toList());

        resp.setAccounts(summaries);
        return resp;
    }
}
