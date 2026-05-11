package com.example.defaultspringsecuritydemo;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class MockMvcIntegrationTests {

    private static final Logger log = LoggerFactory.getLogger(MockMvcIntegrationTests.class);

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "user")
    public void whenLoggedUserRequestsHomePage_ThenSuccess()
            throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/user"))
                .andExpect(status().isOk());

    }

    @Test
    @WithMockUser(username = "user")
    public void whenUserNotAuthorizedToAccessResource_thenRespStatusForbidden()
            throws Exception {

        MvcResult res = mockMvc.perform(MockMvcRequestBuilders.get("/admin"))
                .andExpect(status().isForbidden()).andReturn();

        log.info("_JM_ Response status: {}, content: {}",
                res.getResponse().getStatus(),
                res.getResponse().getContentAsString()
        );

    }

    @Test
    public void whenUserNotAuthenticated_thenRespStatusFoundRedirectingToLoginPage()
            throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/user"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void testActualFormLogin() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "user")
                        .param("password", "password")
                        .with(csrf())) // This adds the hidden CSRF token automatically!
                .andExpect(status().isFound()); // Expect redirect (302) on success
    }

}
