package com.modernbank.dto;

import java.math.BigDecimal;

public class TransactionHistoryItem {
    private Long transactionId;
    private String transactionType;
    private BigDecimal amount;
    private String transactionDate;
    private String referenceNumber;
    private String description;

    public TransactionHistoryItem() {}

    public Long getTransactionId()      { return transactionId; }
    public String getTransactionType()  { return transactionType; }
    public BigDecimal getAmount()       { return amount; }
    public String getTransactionDate()  { return transactionDate; }
    public String getReferenceNumber()  { return referenceNumber; }
    public String getDescription()      { return description; }

    public void setTransactionId(Long transactionId)         { this.transactionId = transactionId; }
    public void setTransactionType(String transactionType)   { this.transactionType = transactionType; }
    public void setAmount(BigDecimal amount)                 { this.amount = amount; }
    public void setTransactionDate(String transactionDate)   { this.transactionDate = transactionDate; }
    public void setReferenceNumber(String referenceNumber)   { this.referenceNumber = referenceNumber; }
    public void setDescription(String description)           { this.description = description; }
}
