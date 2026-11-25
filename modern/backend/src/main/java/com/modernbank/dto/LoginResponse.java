package com.modernbank.dto;

public class LoginResponse {
    private final String token;
    private final String employeeName;

    public LoginResponse(String token, String employeeName) {
        this.token = token;
        this.employeeName = employeeName;
    }

    public String getToken()        { return token; }
    public String getEmployeeName() { return employeeName; }
}
