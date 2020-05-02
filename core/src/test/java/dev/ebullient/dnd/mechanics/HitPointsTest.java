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
package dev.ebullient.dnd.mechanics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HitPointsTest {

    @Test
    public void testStartingHitPointsDescriptionAverage() {
        Assertions.assertEquals(34,
                HitPoints.startingHitPoints("test", "34", Dice.Method.USE_AVERAGE),
                "starting hit points should equal explicit value in string");

        Assertions.assertEquals(59,
                HitPoints.startingHitPoints("test", "59(7d10+21)", Dice.Method.USE_AVERAGE),
                "starting hit points should equal average value in string");
    }

    @Test
    public void testStartingHitPointsDescription() {
        Assertions.assertEquals(34,
                HitPoints.startingHitPoints("test", "34", Dice.Method.ROLL),
                "starting hit points should equal explicit value in string");

        int hp = HitPoints.startingHitPoints("test", "59(1d4+1)", Dice.Method.ROLL);
        Assertions.assertTrue((1 < hp) && (hp < 6),
                "starting hit points should be in the value range, hp=" + hp);
    }

    @Test
    public void testStartingHitPointsSizeCR() {
        int hp = 0;
        int prev = 0;

        // Using fixed averages rather than rolls,
        // hp should increase nicely.
        for (Size size : Size.allValues) {
            prev = 0;
            for (int i = -3; i < 23; i++) {
                hp = HitPoints.startingHitPoints(8, i, size);
                Assertions.assertTrue(hp >= prev,
                        "hit points should ascend. weirdness with size="
                                + size + ", and cr=" + i + "; hp=" + hp + ", and prev=" + prev);
                prev = hp;
            }
        }
    }
}
