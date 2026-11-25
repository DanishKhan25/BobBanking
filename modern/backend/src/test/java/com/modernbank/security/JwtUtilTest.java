package com.modernbank.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void generateAndValidateToken() {
        String token = jwtUtil.generateToken(1L, "admin", "Branch Administrator");
        assertThat(token).isNotBlank();
        assertThat(jwtUtil.isValid(token)).isTrue();
        assertThat(jwtUtil.getUsernameFromToken(token)).isEqualTo("admin");
    }

    @Test
    void invalidTokenReturnsFalse() {
        assertThat(jwtUtil.isValid("not.a.token")).isFalse();
        assertThat(jwtUtil.isValid("")).isFalse();
    }

    @Test
    void claimsContainEmployeeName() {
        String token = jwtUtil.generateToken(1L, "admin", "Branch Administrator");
        var claims = jwtUtil.parseToken(token);
        assertThat(claims.get("employeeName", String.class)).isEqualTo("Branch Administrator");
        assertThat(claims.get("employeeId", Long.class)).isEqualTo(1L);
    }
}
