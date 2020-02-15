/*
 * Copyright © 2019,2020 IBM Corp. All rights reserved.
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

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.ebullient.dnd.beastiary.Beastiary;
import dev.ebullient.dnd.combat.Encounter;
import dev.ebullient.dnd.combat.RoundResult;
import dev.ebullient.dnd.combat.TargetSelector;
import dev.ebullient.dnd.mechanics.Dice;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Timer.Sample;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/combat")
public class CombatController {
    static final Logger logger = LoggerFactory.getLogger(CombatController.class);

    private Beastiary beastiary;
    private CombatMetrics metrics;

    public CombatController(Beastiary beastiary, CombatMetrics metrics) {
        this.beastiary = beastiary;
        this.metrics = metrics;
        logger.debug("Controller initialized bestiary={}, metrics={}", this.beastiary, this.metrics);
    }

    @Timed
    @GetMapping(path = "/any", produces = "application/json")
    private Publisher<RoundResult> any() {
        return go(TargetSelector.SelectAtRandom, Dice.Method.ROLL, Dice.range(5) + 2);
    }

    @Timed
    @GetMapping(path = "/faceoff", produces = "application/json")
    private Publisher<RoundResult> faceoff() {
        return go(TargetSelector.SelectAtRandom, Dice.Method.ROLL, 2);
    }

    @Timed
    @GetMapping(path = "/melee", produces = "application/json")
    private Publisher<RoundResult> melee() {
        return go(TargetSelector.SelectAtRandom, Dice.Method.ROLL, Dice.range(4) + 3);
    }

    Publisher<RoundResult> go(TargetSelector selector, Dice.Method method, int howMany) {

        Encounter encounter = beastiary.buildEncounter()
                .setHowMany(howMany)
                .setTargetSelector(selector)
                .setMethod(method)
                .build();

        return Flux.push(emitter -> {
            Sample eSample = metrics.startEncounter();
            int totalRounds = 0;

            while (!encounter.isFinal()) {
                totalRounds++;
                Sample rSample = metrics.startRound();
                RoundResult result = encounter.oneRound();
                metrics.endRound(rSample, result);

                emitter.next(result);
            }

            emitter.complete();
            metrics.endEncounter(eSample, encounter, totalRounds);
        });
    }
}
