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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import dev.ebullient.dnd.combat.Encounter;
import dev.ebullient.dnd.combat.Encounter.AttackResult;
import dev.ebullient.dnd.combat.Encounter.RoundResult;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Sample;

@Component
class CombatMetrics {
    static final Logger logger = LoggerFactory.getLogger(CombatMetrics.class);

    final MeterRegistry registry;

    public CombatMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public Sample startEncounter() {
        return Timer.start();
    }

    public void endEncounter(Sample sample, Encounter e, int totalRounds) {
        Tags tags = Tags.of(
                "numCombatants", label(e.size()),
                "numTypes", label(e.numTypes()),
                "sizeDelta", label(e.sizeDelta()),
                "crDelta", label(e.crDelta()));

        sample.stop(registry.timer("encounter.duration", tags));
        registry.summary("encounter.rounds", tags).record((double) totalRounds);
    }

    public Sample startRound() {
        return Timer.start();
    }

    public void endRound(Sample sample, RoundResult result) {

        sample.stop(registry.timer("round.duration",
                "numCombatants", label(result.size()),
                "numSurvivors", label(result.getSurvivors().size()),
                "numTypes", label(result.numTypes()),
                "sizeDelta", label(result.sizeDelta()),
                "crDelta", label(result.crDelta())));

        for (AttackResult ar : result.getEvents()) {
            String hitOrMiss = (ar.wasCritical() ? "critical " : "")
                    + (ar.wasSaved() ? "saved " : "")
                    + (ar.wasHit() ? "hit" : "miss");

            registry.summary("round.attacks",
                    "attacker", ar.getAttacker().getName(),
                    "attackType", ar.getType(),
                    "attackName", ar.getName(),
                    "hitOrMiss", hitOrMiss)
                    .record((double) ar.getDamage());
        }
    }

    String label(int value) {
        return String.format("%02d", value);
    }
}
