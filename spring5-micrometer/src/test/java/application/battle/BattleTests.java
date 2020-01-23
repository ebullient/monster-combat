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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import application.monsters.MonsterMaker;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class BattleTests {

    MonsterMaker maker = new MonsterMaker();

    BattleMetrics metrics = new BattleMetrics(new SimpleMeterRegistry());

    @Test
    public void testFaceOffBattle() throws Exception {
        // Randomized content: this should run without failing
        Battle b = new Battle(metrics);
        b.addMonster(maker.make());
        b.addMonster(maker.make());
        b.start().subscribe(i -> System.out.println(i));
    }
}
