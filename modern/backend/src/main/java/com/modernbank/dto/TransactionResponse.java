package com.modernbank.dto;

import java.math.BigDecimal;

public class TransactionResponse {
    private String referenceNumber;
    private BigDecimal newBalance;
    // For transfers
    private BigDecimal fromNewBalance;
    private BigDecimal toNewBalance;

    public TransactionResponse() {}

    // Single-account constructor (deposit/withdrawal)
    public TransactionResponse(String referenceNumber, BigDecimal newBalance) {
        this.referenceNumber = referenceNumber;
        this.newBalance = newBalance;
    }

    // Transfer constructor
    public TransactionResponse(String referenceNumber, BigDecimal fromNewBalance, BigDecimal toNewBalance) {
        this.referenceNumber = referenceNumber;
        this.fromNewBalance = fromNewBalance;
        this.toNewBalance = toNewBalance;
    }

    public String getReferenceNumber()   { return referenceNumber; }
    public BigDecimal getNewBalance()    { return newBalance; }
    public BigDecimal getFromNewBalance(){ return fromNewBalance; }
    public BigDecimal getToNewBalance()  { return toNewBalance; }
}
