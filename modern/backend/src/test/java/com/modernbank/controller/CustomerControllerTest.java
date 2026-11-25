package com.modernbank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modernbank.dto.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    @BeforeEach
    void obtainToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        jwtToken = objectMapper.readTree(body).get("token").asText();
    }

    @Test
    void getCustomerById() throws Exception {
        mockMvc.perform(get("/api/customers/1001")
                   .header("Authorization", "Bearer " + jwtToken))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.customerName").value("John Anderson"))
               .andExpect(jsonPath("$.accounts").isArray());
    }

    @Test
    void getCustomerByAccountNumber() throws Exception {
        mockMvc.perform(get("/api/customers/by-account/1001000001")
                   .header("Authorization", "Bearer " + jwtToken))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.customerName").value("John Anderson"));
    }

    @Test
    void customerNotFound() throws Exception {
        mockMvc.perform(get("/api/customers/9999")
                   .header("Authorization", "Bearer " + jwtToken))
               .andExpect(status().isNotFound());
    }

    @Test
    void accountNotFound() throws Exception {
        mockMvc.perform(get("/api/customers/by-account/NOTEXIST")
                   .header("Authorization", "Bearer " + jwtToken))
               .andExpect(status().isNotFound());
    }
}
