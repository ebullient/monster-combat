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

@Component
class CombatMetrics {
    static final Logger logger = LoggerFactory.getLogger(CombatMetrics.class);

    final MeterRegistry registry;

    public CombatMetrics(MeterRegistry registry) {
        this.registry = registry;
        Dice.setMonitor((k, v) -> registry.summary("dice.rolls", "die", k, "face", label(v)).record((double) v));

        logger.debug("Created CombatMetrics with MeterRegistry: {}", registry);
    }

    public void endEncounter(Encounter e, int totalRounds) {

        registry.summary("encounter.rounds",
                "numCombatants", label(e.getSize()),
                "targetSelector", e.getSelector(),
                "sizeDelta", label(e.getSizeDelta()),
                "crDelta", label(e.getCrDelta()))
                .record((double) totalRounds);
    }

    public void endRound(RoundResult result) {

        for (Event event : result.getEvents()) {
            registry.summary("round.attacks",
                    "attacker", event.getActor().getName(),
                    "attackType", event.getType(),
                    "attackName", event.getName(),
                    "hitOrMiss", event.hitOrMiss(),
                    "targetSelector", result.getSelector())
                    .record((double) event.getDamageAmount());

            registry.counter("attack",
                    "hitOrMiss", event.hitOrMiss(),
                    "attackModifier", label(event.getAttackModifier()),
                    "difficultyClass", label(event.getDifficultyClass()))
                    .increment();
        }
    }

    String label(int value) {
        return String.format("%02d", value);
    }
}
