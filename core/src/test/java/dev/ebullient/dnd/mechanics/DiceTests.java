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
package dev.ebullient.dnd.mechanics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DiceTests {

    @Test
    public void testSingleRollDamage() {
        String roll = "1d20";
        int result = Dice.roll(roll);
        Assertions.assertTrue(result > 0,
                "result should be greater than 0: " + result);
        Assertions.assertTrue(result < 21,
                "result should be less than 21: " + result);
    }

    @Test
    public void testMultipleRollDamage() {
        String roll = "2d20";
        int result = Dice.roll(roll);
        Assertions.assertTrue(result > 0,
                "result should be greater than 0: " + result);
        Assertions.assertTrue(result < 41,
                "result should be less than 41: " + result);
    }

    @Test
    public void testSingleRollMath() {
        String roll = "1d20+6";
        int result = Dice.roll(roll);
        Assertions.assertTrue(result > 6,
                "result should be greater than 6: " + result);
        Assertions.assertTrue(result < 27,
                "result should be less than 27: " + result);
    }

    @Test
    public void testCompoundDamage() {
        String roll = "2d20+2d6";
        int result = Dice.roll(roll);
        Assertions.assertTrue(result > 4,
                "result should be greater than 4: " + result);
        Assertions.assertTrue(result < 53,
                "result should be less than 53: " + result);
    }

    @Test
    public void testAverageRoll() {
        String roll = "15(2d20+2d6)";
        int result = Dice.roll(roll, Dice.Method.USE_AVERAGE);
        Assertions.assertEquals(15, result, roll + " should return average");
    }

    @Test
    public void testOnlyAverageRoll() {
        String roll = "15";
        int result = Dice.roll(roll, Dice.Method.ROLL);
        Assertions.assertEquals(15, result, roll + " should return average");
    }
}
