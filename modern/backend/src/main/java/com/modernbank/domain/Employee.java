package com.modernbank.domain;

import jakarta.persistence.*;

/**
 * Maps to the EMPLOYEES table in legacybank.db.
 * Stores branch employee credentials and name.
 */
@Entity
@Table(name = "EMPLOYEES")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EMPLOYEE_ID")
    private Long employeeId;

    @Column(name = "USERNAME", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "PASSWORD", nullable = false, length = 100)
    private String password;

    @Column(name = "EMPLOYEE_NAME", nullable = false, length = 100)
    private String employeeName;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    protected Employee() {}

    public Long getEmployeeId()       { return employeeId; }
    public String getUsername()       { return username; }
    public String getPassword()       { return password; }
    public String getEmployeeName()   { return employeeName; }
    public String getStatus()         { return status; }

    public void setPassword(String password) { this.password = password; }
}
