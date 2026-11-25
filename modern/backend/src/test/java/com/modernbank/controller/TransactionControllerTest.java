package com.modernbank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TransactionControllerTest {

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

    // ── Deposit ──────────────────────────────────────────────────────────────

    @Test
    void depositSuccess() throws Exception {
        mockMvc.perform(post("/api/transactions/deposit")
                   .header("Authorization", "Bearer " + jwtToken)
                   .contentType(MediaType.APPLICATION_JSON)
                   .content("{\"accountNumber\":\"1001000001\",\"amount\":100.00}"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.referenceNumber").value(org.hamcrest.Matchers.startsWith("DEP")))
               .andExpect(jsonPath("$.newBalance").isNumber());
    }

    @Test
    void depositFrozenAccount() throws Exception {
        mockMvc.perform(post("/api/transactions/deposit")
                   .header("Authorization", "Bearer " + jwtToken)
                   .contentType(MediaType.APPLICATION_JSON)
                   .content("{\"accountNumber\":\"1001000008\",\"amount\":100.00}"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("not active")));
    }

    @Test
    void depositInvalidAmount() throws Exception {
        mockMvc.perform(post("/api/transactions/deposit")
                   .header("Authorization", "Bearer " + jwtToken)
                   .contentType(MediaType.APPLICATION_JSON)
                   .content("{\"accountNumber\":\"1001000001\",\"amount\":-50}"))
               .andExpect(status().isBadRequest());
    }

    // ── Withdrawal ───────────────────────────────────────────────────────────

    @Test
    void withdrawalSuccess() throws Exception {
        mockMvc.perform(post("/api/transactions/withdrawal")
                   .header("Authorization", "Bearer " + jwtToken)
                   .contentType(MediaType.APPLICATION_JSON)
                   .content("{\"accountNumber\":\"1001000001\",\"amount\":50.00}"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.referenceNumber").value(org.hamcrest.Matchers.startsWith("WDR")));
    }

    @Test
    void withdrawalInsufficientFunds() throws Exception {
        mockMvc.perform(post("/api/transactions/withdrawal")
                   .header("Authorization", "Bearer " + jwtToken)
                   .contentType(MediaType.APPLICATION_JSON)
                   .content("{\"accountNumber\":\"1001000010\",\"amount\":99999.00}"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Insufficient")));
    }

    // ── Transfer ─────────────────────────────────────────────────────────────

    @Test
    void transferSuccess() throws Exception {
        mockMvc.perform(post("/api/transactions/transfer")
                   .header("Authorization", "Bearer " + jwtToken)
                   .contentType(MediaType.APPLICATION_JSON)
                   .content("{\"fromAccount\":\"1001000003\",\"toAccount\":\"1001000004\",\"amount\":100.00}"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.referenceNumber").value(org.hamcrest.Matchers.startsWith("TRF")))
               .andExpect(jsonPath("$.fromNewBalance").isNumber())
               .andExpect(jsonPath("$.toNewBalance").isNumber());
    }

    @Test
    void transferSameAccount() throws Exception {
        mockMvc.perform(post("/api/transactions/transfer")
                   .header("Authorization", "Bearer " + jwtToken)
                   .contentType(MediaType.APPLICATION_JSON)
                   .content("{\"fromAccount\":\"1001000001\",\"toAccount\":\"1001000001\",\"amount\":100.00}"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("different")));
    }

    @Test
    void transferInsufficientFunds() throws Exception {
        mockMvc.perform(post("/api/transactions/transfer")
                   .header("Authorization", "Bearer " + jwtToken)
                   .contentType(MediaType.APPLICATION_JSON)
                   .content("{\"fromAccount\":\"1001000010\",\"toAccount\":\"1001000001\",\"amount\":99999.00}"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Insufficient")));
    }

    @Test
    void transferToFrozenAccount() throws Exception {
        mockMvc.perform(post("/api/transactions/transfer")
                   .header("Authorization", "Bearer " + jwtToken)
                   .contentType(MediaType.APPLICATION_JSON)
                   .content("{\"fromAccount\":\"1001000001\",\"toAccount\":\"1001000008\",\"amount\":100.00}"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("not active")));
    }

    // ── History ──────────────────────────────────────────────────────────────

    @Test
    void transactionHistory() throws Exception {
        mockMvc.perform(get("/api/transactions/1001000001")
                   .header("Authorization", "Bearer " + jwtToken))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray());
    }

    @Test
    void transactionHistoryWithTypeFilter() throws Exception {
        mockMvc.perform(get("/api/transactions/1001000001?type=DEPOSIT")
                   .header("Authorization", "Bearer " + jwtToken))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray());
    }

    @Test
    void transactionHistoryAccountNotFound() throws Exception {
        mockMvc.perform(get("/api/transactions/NOTEXIST")
                   .header("Authorization", "Bearer " + jwtToken))
               .andExpect(status().isNotFound());
    }

    // ── Dashboard ────────────────────────────────────────────────────────────

    @Test
    void dashboardSummary() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary")
                   .header("Authorization", "Bearer " + jwtToken))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalCustomers").isNumber())
               .andExpect(jsonPath("$.totalAccounts").isNumber())
               .andExpect(jsonPath("$.totalBalance").isNumber())
               .andExpect(jsonPath("$.frozenAccounts").isNumber());
    }
}
