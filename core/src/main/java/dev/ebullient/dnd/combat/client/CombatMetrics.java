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
package dev.ebullient.dnd.combat.client;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ebullient.dnd.combat.Encounter;
import dev.ebullient.dnd.combat.RoundResult;
import dev.ebullient.dnd.combat.RoundResult.Event;
import dev.ebullient.dnd.mechanics.Dice;
import io.micrometer.core.instrument.MeterRegistry;

public class CombatMetrics {
    static final Logger logger = LoggerFactory.getLogger(CombatMetrics.class);

    final MeterRegistry registry;
    final AtomicInteger last_roll;
    final AtomicInteger last_felled;

    public CombatMetrics(MeterRegistry registry) {
        this.registry = registry;

        last_felled = registry.gauge("last.felled", new AtomicInteger(0));
        last_roll = registry.gauge("last.roll", new AtomicInteger(0));

        Dice.setMonitor((k, v) -> {
            registry.counter("dice.rolls", "die", k, "face", label(v)).increment();
            last_roll.set(v);
        });

        logger.debug("Created CombatMetrics with MeterRegistry: {}", registry);
    }

    public void endEncounter(Encounter e, int totalRounds) {
        registry.summary("encounter.rounds",
                "numCombatants", label(e.getNumCombatants()),
                "targetSelector", e.getSelector(),
                "sizeDelta", label(e.getSizeDelta()))
                .record((double) totalRounds);
    }

    public void endRound(RoundResult result) {
        for (Event event : result.getEvents()) {
            registry.summary("round.attacks",
                    "hitOrMiss", event.hitOrMiss(),
                    "attackType", event.getAttackType(),
                    "damageType", event.getType())
                    .record((double) event.getDamageAmount());

            registry.summary("attacker.damage",
                    "attacker", event.getActor().getName(),
                    "attackName", event.getName(),
                    "hitOrMiss", event.hitOrMiss())
                    .record((double) event.getDamageAmount());

            registry.summary("attack.success",
                    "attackType", event.getAttackType(),
                    "hitOrMiss", event.hitOrMiss())
                    .record((double) event.getDifficultyClass());
        }

        last_felled.set(result.getNumCombatants() - result.getSurvivors().size());
    }

    String label(int value) {
        return String.format("%02d", value);
    }
}
