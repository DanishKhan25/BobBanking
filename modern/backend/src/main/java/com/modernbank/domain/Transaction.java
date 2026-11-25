package com.modernbank.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Maps to the TRANSACTIONS table in legacybank.db.
 * ACCOUNT_NUMBER is both the FK column and the join column.
 */
@Entity
@Table(name = "TRANSACTIONS")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRANSACTION_ID")
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT_NUMBER", referencedColumnName = "ACCOUNT_NUMBER", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "TRANSACTION_TYPE", nullable = false, length = 20)
    private TransactionType transactionType;

    @Column(name = "AMOUNT", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "DESCRIPTION", length = 250)
    private String description;

    @Column(name = "TRANSACTION_DATE", nullable = false)
    private String transactionDate;

    @Column(name = "REFERENCE_NUMBER", nullable = false, length = 30)
    private String referenceNumber;

    public Transaction() {}

    public Long getTransactionId()            { return transactionId; }
    public Account getAccount()               { return account; }
    public TransactionType getTransactionType() { return transactionType; }
    public BigDecimal getAmount()             { return amount; }
    public String getDescription()            { return description; }
    public String getTransactionDate()        { return transactionDate; }
    public String getReferenceNumber()        { return referenceNumber; }

    public void setTransactionId(Long id)         { this.transactionId = id; }
    public void setAccount(Account account)        { this.account = account; }
    public void setTransactionType(TransactionType t) { this.transactionType = t; }
    public void setAmount(BigDecimal amount)       { this.amount = amount; }
    public void setDescription(String description) { this.description = description; }
    public void setTransactionDate(String date)    { this.transactionDate = date; }
    public void setReferenceNumber(String ref)     { this.referenceNumber = ref; }

    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER_DEBIT, TRANSFER_CREDIT
    }
}
