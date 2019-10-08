/*
 * Copyright © 2019 IBM Corp. All rights reserved.
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
package application.battle;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import application.battle.Round.Result;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Sample;


@Component
class BattleMetrics {
    static final Logger logger = LoggerFactory.getLogger(BattleMetrics.class);

    MeterRegistry registry;
    AtomicInteger activeRoundsGauge = new AtomicInteger(0);
    AtomicInteger activeMeleeGauge = new AtomicInteger(0);
    AtomicInteger activeFaceOffGauge = new AtomicInteger(0);
    AtomicInteger engagedMonsters = new AtomicInteger(0);

    DistributionSummary numberOfFaceOffRounds;
    DistributionSummary numberOfMeleeRounds;

    Timer faceoffDuration;
    Timer meleeDuration;
    Timer roundDuration;

    public BattleMetrics(MeterRegistry registry) {
        this.registry = registry;

        registry.gauge("battle.rounds.active", activeRoundsGauge);
        registry.gauge("battles.melee.active", activeMeleeGauge);
        registry.gauge("battles.faceoff.active", activeFaceOffGauge);
        registry.gauge("monsters.engaged", engagedMonsters);

        numberOfFaceOffRounds = DistributionSummary.builder("battles.rounds")
            .tag("type", "faceoff")
            .description("Number of rounds in faceoff battles")
            .register(registry);

        numberOfMeleeRounds = DistributionSummary.builder("battles.rounds")
            .description("Number of rounds in melee battles")
            .tag("type", "melee")
            .register(registry);

        roundDuration = Timer.builder("battle.rounds.duration")
            .minimumExpectedValue(Duration.ofMillis(1))
            .maximumExpectedValue(Duration.ofSeconds(1))
            .register(registry);
        faceoffDuration = Timer.builder("battles.duration")
            .tags("type", "faceoff")
            .minimumExpectedValue(Duration.ofMillis(1))
            .maximumExpectedValue(Duration.ofSeconds(30))
            .register(registry);
        meleeDuration = Timer.builder("battles.duration")
            .tags("type", "melee")
            .minimumExpectedValue(Duration.ofMillis(1))
            .maximumExpectedValue(Duration.ofSeconds(30))
            .register(registry);

        logger.debug("Battle metrics initialized, registry={}", this.registry);
    }

    public Sample startBattle(Battle b) {
        if ( b.participants.size() == 2 ) {
            activeFaceOffGauge.incrementAndGet();
        } else {
            activeMeleeGauge.incrementAndGet();
        }
        return Timer.start();
    }

    public void finishBattle(Sample sample, Battle b, Round finalRound) {
        if ( b.participants.size() == 2 ) {
            sample.stop(faceoffDuration);
            registry.counter("battles.completed", "type", "faceoff").increment();
            activeFaceOffGauge.decrementAndGet();
            numberOfFaceOffRounds.record((double) finalRound.getNumber());
        } else {
            sample.stop(meleeDuration);
            registry.counter("battles.completed", "type", "melee").increment();
            activeMeleeGauge.decrementAndGet();
            numberOfMeleeRounds.record((double) finalRound.getNumber());
        }

        // disengage the last monster
        engagedMonsters.decrementAndGet();
    }

    public Sample startRound(Round r) {
        activeRoundsGauge.incrementAndGet();
        return Timer.start();
    }

    public void attackDamage(Participant attacker, Participant target, Result r) {
        registry.counter("monster.attacks",
                    "type", r.isHit() ? "hit" : "miss",
                    "critical", Boolean.toString(r.isCritical()))
            .increment();

        registry.summary("monster.attack.damage",
                "type", attacker.getType(),
                "size", attacker.getSize())
            .record((double) r.getDamage());

        registry.summary("weapon.attack.damage",
                "type", r.getAttackName())
            .record((double) r.getDamage());
    }

    public void finishRound(Sample sample, Round r) {
        activeRoundsGauge.decrementAndGet();

        // There may not be any surprised monsters
        if ( r.participants.size() <= 0 ) {
            return;
        }

        sample.stop(roundDuration);

        for(Participant p : r.participants ) {
            if ( p.isAlive() ) {
                registry.counter("monster.rounds.survived",
                        "type", p.getType(),
                        "size", p.getSize())
                    .increment();
                registry.counter("monster.rounds.survived.individual",
                        "name", p.getName())
                    .increment();
        } else {
                engagedMonsters.decrementAndGet();
            }

            if ( p == r.getVictor() ) {
                registry.counter("monster.rounds.won",
                        "type", p.getType(),
                        "size", p.getSize())
                    .increment();

                registry.counter("monster.rounds.won.individual",
                        "name", p.getName())
                    .increment();
            }

            if ( p.isSurprised() ) {
                registry.counter("monster.surprised",
                        "type", p.getType(),
                        "size", p.getSize())
                    .increment();
                registry.counter("monster.surprised.individual",
                        "name", p.getName())
                    .increment();
            }
        }
    }
}
