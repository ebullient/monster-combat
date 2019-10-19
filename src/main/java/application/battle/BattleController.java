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

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import application.mechanics.Dice;
import application.monsters.Beastiary;
import io.micrometer.core.annotation.Timed;

@RestController
@RequestMapping("/battle")
public class BattleController {
    static final Logger logger = LoggerFactory.getLogger(BattleController.class);

    private Beastiary beastiary;
    private BattleMetrics metrics;

    public BattleController(Beastiary beastiary, BattleMetrics metrics) {
        this.beastiary = beastiary;
        this.metrics = metrics;
        logger.debug("Battle controller initialized bestiary={}, metrics={}", this.beastiary, this.metrics);
    }

    @Timed
    @GetMapping(path = "/faceoff", produces = "application/json")
    private Publisher<Round> faceoff() {
        Battle battle = new Battle(metrics);
        battle.addMonster(beastiary.getRandomMonster());
        battle.addMonster(beastiary.getRandomMonster());

        return battle.start();
    }

    @Timed
    @GetMapping(path = "/melee", produces = "application/json")
    private Publisher<Round> melee() {
        int n = Dice.range(3) + 3;
        Battle battle = new Battle(metrics);
        for ( int i = 0; i < n; i++) {
            battle.addMonster(beastiary.getRandomMonster());
        }

        return battle.start();
    }
}
