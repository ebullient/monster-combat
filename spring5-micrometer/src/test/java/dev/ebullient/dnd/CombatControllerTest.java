/*
 * Copyright © 2020 IBM Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
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
