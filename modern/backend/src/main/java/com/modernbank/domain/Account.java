package com.modernbank.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Maps to the ACCOUNTS table in legacybank.db.
 */
@Entity
@Table(name = "ACCOUNTS")
public class Account {

    @Id
    @Column(name = "ACCOUNT_ID")
    private Long accountId;

    @Column(name = "ACCOUNT_NUMBER", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "ACCOUNT_TYPE", nullable = false, length = 20)
    private AccountType accountType;

    @Column(name = "BALANCE", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private AccountStatus status;

    @Column(name = "CREATED_DATE", nullable = false)
    private String createdDate;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    protected Account() {}

    public Long getAccountId()          { return accountId; }
    public String getAccountNumber()    { return accountNumber; }
    public Customer getCustomer()       { return customer; }
    public AccountType getAccountType() { return accountType; }
    public BigDecimal getBalance()      { return balance; }
    public AccountStatus getStatus()    { return status; }
    public String getCreatedDate()      { return createdDate; }
    public List<Transaction> getTransactions() { return transactions; }

    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public enum AccountType {
        SAVINGS, CHECKING, FIXED_DEPOSIT
    }

    public enum AccountStatus {
        ACTIVE, FROZEN, CLOSED
    }
}
