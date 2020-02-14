package dev.ebullient.dnd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(classes = Main.class)
@ExtendWith(SpringExtension.class)
@AutoConfigureWebTestClient
public class MainTest {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    CombatMetrics combatMetrics;

    @Test
    public void testHealthEndpoint() {
        webClient.get().uri("/actuator/health")
                .header(HttpHeaders.ACCEPT, "application/json")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    public void testLivenessEndpoint() {
        webClient.get().uri("/actuator/liveness")
                .header(HttpHeaders.ACCEPT, "application/json")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    public void testMetricsEndpoint() {
        testLivenessEndpoint(); // access a page

        webClient.get().uri("/actuator/metrics")
                .header(HttpHeaders.ACCEPT, "application/json")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.names").isArray();
    }

    @Test
    public void testPrometheusEndpoint() {
        testLivenessEndpoint(); // access a page

        webClient.get().uri("/actuator/prometheus")
                .header(HttpHeaders.ACCEPT, "text/plain")
                .exchange()
                .expectStatus().isOk();
    }
}
