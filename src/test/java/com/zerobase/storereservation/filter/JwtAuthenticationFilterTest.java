package com.zerobase.storereservation.filter;

import com.zerobase.storereservation.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("JWT 필터 - 올바른 토큰으로 인증 요청")
    void testFilterWithValidToken() throws Exception {
        //given
        String token = jwtUtil.generateToken("testUser");

        //when&given
        mockMvc.perform(get("/api/protected-endpoint")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(result -> assertEquals("Access Granted",
                        result.getResponse().getContentAsString()));
    }

    @Test
    @DisplayName("JWT 필터 - 토큰 없이 인증 요청 실패")
    void testFilterWithoutToken() throws Exception {
        //when&then
        mockMvc.perform(get("/api/protected-endpoint"))
                .andExpect(status().isUnauthorized());
    }
}