package com.modernbank.dto;

import java.math.BigDecimal;
import java.util.List;

public class CustomerResponse {
    private Long customerId;
    private String customerName;
    private String email;
    private String phone;
    private String address;
    private String createdDate;
    private List<AccountSummary> accounts;

    public CustomerResponse() {}

    public Long getCustomerId()          { return customerId; }
    public String getCustomerName()      { return customerName; }
    public String getEmail()             { return email; }
    public String getPhone()             { return phone; }
    public String getAddress()           { return address; }
    public String getCreatedDate()       { return createdDate; }
    public List<AccountSummary> getAccounts() { return accounts; }

    public void setCustomerId(Long customerId)          { this.customerId = customerId; }
    public void setCustomerName(String customerName)    { this.customerName = customerName; }
    public void setEmail(String email)                  { this.email = email; }
    public void setPhone(String phone)                  { this.phone = phone; }
    public void setAddress(String address)              { this.address = address; }
    public void setCreatedDate(String createdDate)      { this.createdDate = createdDate; }
    public void setAccounts(List<AccountSummary> accs)  { this.accounts = accs; }

    public static class AccountSummary {
        private String accountNumber;
        private String accountType;
        private String status;
        private BigDecimal balance;

        public AccountSummary() {}

        public String getAccountNumber() { return accountNumber; }
        public String getAccountType()   { return accountType; }
        public String getStatus()        { return status; }
        public BigDecimal getBalance()   { return balance; }

        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        public void setAccountType(String accountType)     { this.accountType = accountType; }
        public void setStatus(String status)               { this.status = status; }
        public void setBalance(BigDecimal balance)         { this.balance = balance; }
    }
}
