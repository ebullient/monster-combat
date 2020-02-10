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
package dev.ebullient.dnd;

import java.util.ArrayList;
import java.util.List;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.ebullient.dnd.beastiary.Beast;
import dev.ebullient.dnd.beastiary.Beastiary;
import dev.ebullient.dnd.combat.Encounter;
import dev.ebullient.dnd.combat.Encounter.RoundResult;
import dev.ebullient.dnd.combat.TargetSelector;
import dev.ebullient.dnd.mechanics.Dice;
import io.micrometer.core.annotation.Timed;
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
    @GetMapping(path = "/faceoff", produces = "application/json")
    private Publisher<RoundResult> faceoff() {

        List<Beast> monsters = new ArrayList<>();
        monsters.add(beastiary.findOne());
        monsters.add(beastiary.findOne());

        return go(TargetSelector.SelectAtRandom, monsters);
    }

    @Timed
    @GetMapping(path = "/melee", produces = "application/json")
    private Publisher<RoundResult> melee() {

        List<Beast> monsters = new ArrayList<>();
        int n = Dice.range(4) + 3;
        for (int i = 0; i < n; i++) {
            monsters.add(beastiary.findOne());
        }

        return go(TargetSelector.SelectAtRandom, monsters);
    }

    Publisher<RoundResult> go(TargetSelector selector, List<Beast> monsters) {
        Encounter encounter = new Encounter(selector, Dice.Method.ROLL, monsters);

        return Flux.push(emitter -> {
            metrics.startEncounter();
            int totalRounds = 0;

            while (!encounter.isFinal()) {
                RoundResult result = encounter.oneRound();
                emitter.next(result);
            }

            metrics.endEncounter(totalRounds);
            emitter.complete();
        });
    }
}
