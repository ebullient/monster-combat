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
import dev.ebullient.dnd.combat.RoundResult;
import dev.ebullient.dnd.combat.RoundResult.Event;
import dev.ebullient.dnd.mechanics.Dice;
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
        Dice.setMonitor((k, v) -> registry.summary("dice.rolls", "die", k, "face", label(v)).record((double) v));
    }

    public Sample startEncounter() {
        return Timer.start();
    }

    public void endEncounter(Sample sample, Encounter e, int totalRounds) {
        Tags tags = Tags.of(
                "numCombatants", label(e.getSize()),
                "numTypes", label(e.getNumTypes()),
                "sizeDelta", label(e.getSizeDelta()),
                "crDelta", label(e.getCrDelta()));

        sample.stop(registry.timer("encounter.duration", tags));
        registry.summary("encounter.rounds", tags).record((double) totalRounds);
    }

    public Sample startRound() {
        return Timer.start();
    }

    public void endRound(Sample sample, RoundResult result) {

        sample.stop(registry.timer("round.duration",
                "numSurvivors", label(result.getSurvivors().size()),
                "numCombatants", label(result.getSize()),
                "sizeDelta", label(result.getSizeDelta()),
                "crDelta", label(result.getCrDelta())));

        for (Event event : result.getEvents()) {
            String hitOrMiss = (event.isCritical() ? "critical " : "")
                    + (event.isSaved() ? "saved " : "")
                    + (event.isHit() ? "hit" : "miss");

            registry.summary("round.attacks",
                    "attacker", event.getActor().getName(),
                    "attackType", event.getType(),
                    "attackName", event.getName(),
                    "hitOrMiss", hitOrMiss)
                    .record((double) event.getDamageAmount());
        }
    }

    String label(int value) {
        return String.format("%02d", value);
    }
}
