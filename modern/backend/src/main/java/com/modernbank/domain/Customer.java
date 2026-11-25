package com.modernbank.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Maps to the CUSTOMERS table in legacybank.db.
 */
@Entity
@Table(name = "CUSTOMERS")
public class Customer {

    @Id
    @Column(name = "CUSTOMER_ID")
    private Long customerId;

    @Column(name = "CUSTOMER_NAME", nullable = false, length = 100)
    private String customerName;

    @Column(name = "EMAIL", length = 100)
    private String email;

    @Column(name = "PHONE", length = 30)
    private String phone;

    @Column(name = "ADDRESS", length = 250)
    private String address;

    @Column(name = "CREATED_DATE", nullable = false)
    private String createdDate;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<Account> accounts = new ArrayList<>();

    protected Customer() {}

    public Long getCustomerId()     { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getEmail()        { return email; }
    public String getPhone()        { return phone; }
    public String getAddress()      { return address; }
    public String getCreatedDate()  { return createdDate; }
    public List<Account> getAccounts() { return accounts; }
}
