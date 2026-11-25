package com.modernbank.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class TransferRequest {

    @NotBlank(message = "Source account number is required")
    private String fromAccount;

    @NotBlank(message = "Destination account number is required")
    private String toAccount;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @DecimalMax(value = "10000000.00", message = "Amount must not exceed 10,000,000")
    @Digits(integer = 10, fraction = 2, message = "Amount must have at most 2 decimal places")
    private BigDecimal amount;

    private String description;

    public String getFromAccount()   { return fromAccount; }
    public String getToAccount()     { return toAccount; }
    public BigDecimal getAmount()    { return amount; }
    public String getDescription()   { return description; }

    public void setFromAccount(String fromAccount)   { this.fromAccount = fromAccount; }
    public void setToAccount(String toAccount)       { this.toAccount = toAccount; }
    public void setAmount(BigDecimal amount)          { this.amount = amount; }
    public void setDescription(String description)    { this.description = description; }
}
