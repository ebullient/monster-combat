package dev.ebullient.dnd;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

import dev.ebullient.dnd.combat.client.CombatMetrics;
import dev.ebullient.dnd.combat.client.RoundResult;

@Import(Configuration.class)
@WebFluxTest(controllers = CombatController.class)
public class CombatControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    CombatMetrics combatMetrics;

    @Test
    public void testFaceoffEndpoint() {
        webClient.get().uri("/combat/faceoff")
                .header(HttpHeaders.ACCEPT, "application/json")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RoundResult.class);
    }

    @Test
    public void testMeleeEndpoint() {
        webClient.get().uri("/combat/melee")
                .header(HttpHeaders.ACCEPT, "application/json")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RoundResult.class);
    }

    @Test
    public void testAnyEndpoint() {
        webClient.get().uri("/combat/any")
                .header(HttpHeaders.ACCEPT, "application/json")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RoundResult.class);
    }
}
