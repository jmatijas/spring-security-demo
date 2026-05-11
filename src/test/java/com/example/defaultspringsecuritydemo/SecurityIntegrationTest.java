package com.example.defaultspringsecuritydemo;


import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
public class SecurityIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(SecurityIntegrationTest.class);

    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    TestRestTemplate restTemplate;

    URL base;

    @LocalServerPort
    int port;

    @BeforeAll
    static void beforeAll() throws MalformedURLException {
        redis.start();
    }

    @BeforeEach
    public void setUp() throws MalformedURLException {
        base = new URL("http://localhost:" + port);

        // Configure a HttpClient that does NOT follow redirects.
        // Requires adding dependency httpclient5 (if not already brought by a starter).
        // Spring Boot automatically detects HttpClient 5 dependency and allows usage of
        // HttpComponentsClientHttpRequestFactory.
        CloseableHttpClient httpClient = HttpClients.custom()
                .disableRedirectHandling()
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        restTemplate = new TestRestTemplate(new RestTemplateBuilder().requestFactory(() -> requestFactory));
    }

    @Test
    public void whenUnauthorizedUserAccessPublicResource_ThenSuccess() {

        ResponseEntity<String> response = restTemplate.getForEntity(base + "/public", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).isEqualTo("Hello Public!");
    }

    @Test
    public void whenLoggedUserRequestsHomePage_ThenSuccess() {
        // Manually simulate the form login POST
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", "user");
        form.add("password", "password");

        // Perform the login
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(base.toString() + "/login", form, String.class);

        // Form login redirects on success (302) to the default target page
        assertEquals(HttpStatus.FOUND, loginResponse.getStatusCode());

        // Extract the Session Cookie
        String sessionCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        log.info("Session cookie is [{}]", sessionCookie);

        // Use the cookie to access the home page
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, sessionCookie);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(base.toString(), HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).isEqualTo("Hello World!");
    }

    @Test
    public void whenUserWithWrongCredentials_thenLoginPageWithErrorUriParam() {

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", "user");
        form.add("password", "wrongpassword");

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(base.toString() + "/login", form, String.class);

        // Form login redirects (302) to the error target page
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(loginResponse.getHeaders().getFirst(HttpHeaders.LOCATION)).contains("/login?error");
    }

    @Test
    public void whenUserNotAuthorized_thenForbiddenResponse() {

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", "user");
        form.add("password", "password");

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(base.toString() + "/login", form, String.class);

        assertEquals(HttpStatus.FOUND, loginResponse.getStatusCode());

        // Extract the Session Cookie
        String sessionCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        log.info("Session cookie is [{}]", sessionCookie);

        // Use the cookie to access the home page
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, sessionCookie);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(base.toString() + "/admin", HttpMethod.GET, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("Forbidden");
    }
}