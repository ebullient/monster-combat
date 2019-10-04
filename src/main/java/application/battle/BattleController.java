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

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.reactivestreams.Publisher;

import application.mechanics.Dice;
import application.monsters.Beastiary;

@RestController
@RequestMapping("/battle")
public class BattleController {

    private Beastiary beastiary;

    public BattleController(Beastiary beastiary) {
        this.beastiary = beastiary;
    }

    @GetMapping(path = "/faceoff", produces = "application/json")
    private Publisher<Round> faceoff() {
        int n = Dice.range(3) + 2;
        Battle battle = new Battle();
        for ( int i = 0; i < n; i++) {
            battle.addMonster(beastiary.getRandomMonster());
        }

        return battle.start();
    }
}
