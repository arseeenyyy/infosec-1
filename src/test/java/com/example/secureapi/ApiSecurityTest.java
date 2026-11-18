package com.example.secureapi;

import com.example.secureapi.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiSecurityTest {

    private static final String PASSWORD = "StrongPass123";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired UserRepository users;

    private String newUser() throws Exception {
        String name = "u" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + name + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isCreated());
        return name;
    }

    private String login(String name, String password) throws Exception {
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + name + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return om.readTree(body).get("token").asText();
    }

    @Test
    void dataWithoutToken_isUnauthorized() throws Exception {
        mvc.perform(get("/api/data")).andExpect(status().isUnauthorized());
    }

    @Test
    void dataWithGarbageToken_isUnauthorized() throws Exception {
        mvc.perform(get("/api/data").header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithWrongPassword_isUnauthorized() throws Exception {
        String name = newUser();
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + name + "\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerLoginAndAccessData_works() throws Exception {
        String token = login(newUser(), PASSWORD);
        mvc.perform(get("/api/data").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void passwordIsStoredAsBcryptHash() throws Exception {
        String name = newUser();
        String hash = users.findByUsername(name).orElseThrow().getPasswordHash();
        assertTrue(hash.startsWith("$2"), "ожидается bcrypt-хэш");
        assertFalse(hash.contains(PASSWORD));
    }

    @Test
    void xssPayloadIsEscaped() throws Exception {
        String token = login(newUser(), PASSWORD);
        mvc.perform(post("/api/posts").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"xss\",\"content\":\"<script>alert(1)</script>\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("&lt;script&gt;")))
                .andExpect(content().string(not(containsString("<script>"))));
    }

    @Test
    void sqlInjectionPayloadIsHarmless() throws Exception {
        String token = login(newUser(), PASSWORD);
        mvc.perform(get("/api/data").param("search", "' OR '1'='1' --")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
