package com.modernbank.dto;

import java.math.BigDecimal;

public class AccountDetailsResponse {
    private String accountNumber;
    private String accountType;
    private String status;
    private BigDecimal balance;
    private String openedDate;
    private Long customerId;
    private String customerName;
    private String email;
    private String phone;

    public AccountDetailsResponse() {}

    public String getAccountNumber() { return accountNumber; }
    public String getAccountType()   { return accountType; }
    public String getStatus()        { return status; }
    public BigDecimal getBalance()   { return balance; }
    public String getOpenedDate()    { return openedDate; }
    public Long getCustomerId()      { return customerId; }
    public String getCustomerName()  { return customerName; }
    public String getEmail()         { return email; }
    public String getPhone()         { return phone; }

    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setAccountType(String accountType)     { this.accountType = accountType; }
    public void setStatus(String status)               { this.status = status; }
    public void setBalance(BigDecimal balance)         { this.balance = balance; }
    public void setOpenedDate(String openedDate)       { this.openedDate = openedDate; }
    public void setCustomerId(Long customerId)         { this.customerId = customerId; }
    public void setCustomerName(String customerName)   { this.customerName = customerName; }
    public void setEmail(String email)                 { this.email = email; }
    public void setPhone(String phone)                 { this.phone = phone; }
}
