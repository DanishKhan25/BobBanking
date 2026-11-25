package com.modernbank.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/api/health"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void loginSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.token").isNotEmpty())
               .andExpect(jsonPath("$.employeeName").value("Branch Administrator"));
    }

    @Test
    void loginWrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content("{\"username\":\"admin\",\"password\":\"wrongpassword\"}"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void loginUnknownUser() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content("{\"username\":\"nobody\",\"password\":\"admin123\"}"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointRequiresToken() throws Exception {
        mockMvc.perform(get("/api/accounts/1001000001"))
               .andExpect(status().isUnauthorized());
    }
}
