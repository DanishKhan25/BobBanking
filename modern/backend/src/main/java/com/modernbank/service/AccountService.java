package com.modernbank.service;

import com.modernbank.domain.Account;
import com.modernbank.dto.AccountDetailsResponse;
import com.modernbank.dto.DashboardSummary;
import com.modernbank.repository.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountDetailsResponse findByAccountNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + accountNumber));
        return toResponse(account);
    }

    public DashboardSummary getDashboardSummary() {
        DashboardSummary summary = new DashboardSummary();
        summary.setTotalCustomers(accountRepository.countDistinctCustomers());
        summary.setTotalAccounts(accountRepository.count());
        summary.setTotalBalance(accountRepository.sumAllBalances());
        summary.setFrozenAccounts(accountRepository.countByStatus(Account.AccountStatus.FROZEN));
        return summary;
    }

    private AccountDetailsResponse toResponse(Account account) {
        AccountDetailsResponse resp = new AccountDetailsResponse();
        resp.setAccountNumber(account.getAccountNumber());
        resp.setAccountType(account.getAccountType().name());
        resp.setStatus(account.getStatus().name());
        resp.setBalance(account.getBalance());
        // Keep only date portion for display: "2024-01-15 10:30:00" → "2024-01-15"
        String opened = account.getCreatedDate();
        resp.setOpenedDate(opened != null && opened.length() >= 10 ? opened.substring(0, 10) : opened);
        resp.setCustomerId(account.getCustomer().getCustomerId());
        resp.setCustomerName(account.getCustomer().getCustomerName());
        resp.setEmail(account.getCustomer().getEmail() != null ? account.getCustomer().getEmail() : "-");
        resp.setPhone(account.getCustomer().getPhone() != null ? account.getCustomer().getPhone() : "-");
        return resp;
    }
}
