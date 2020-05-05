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
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import dev.ebullient.dnd.combat.client.CombatMetrics;

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
