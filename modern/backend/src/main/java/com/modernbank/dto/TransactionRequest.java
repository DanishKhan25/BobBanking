package com.modernbank.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class TransactionRequest {

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @DecimalMax(value = "10000000.00", message = "Amount must not exceed 10,000,000")
    @Digits(integer = 10, fraction = 2, message = "Amount must have at most 2 decimal places")
    private BigDecimal amount;

    private String description;

    public String getAccountNumber() { return accountNumber; }
    public BigDecimal getAmount()    { return amount; }
    public String getDescription()   { return description; }

    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setAmount(BigDecimal amount)           { this.amount = amount; }
    public void setDescription(String description)     { this.description = description; }
}
