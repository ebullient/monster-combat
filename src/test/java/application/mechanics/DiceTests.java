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
package application.mechanics;

import org.junit.Assert;
import org.junit.Test;

public class DiceTests {

    @Test
    public void singleRollDamage() {
        String roll = "1d20";
        int result = Dice.roll(roll);
        Assert.assertTrue("result should be greater than 0: " + result,
            result > 0);
        Assert.assertTrue("result should be less than 21: " + result,
            result < 21);
    }

    @Test
    public void nSingleRollDamage() {
        String roll = "2d20";
        int result = Dice.roll(roll);
        Assert.assertTrue("result should be greater than 0: " + result,
            result > 0);
        Assert.assertTrue("result should be less than 41: " + result,
            result < 41);
    }

    @Test
    public void singleRollMath() {
        String roll = "1d20+6";
        int result = Dice.roll(roll);
        Assert.assertTrue("result should be greater than 6: " + result,
            result > 6);
        Assert.assertTrue("result should be less than 27: " + result,
            result < 27);
    }

    @Test
    public void multiDamage() {
        String roll = "2d20+2d6";
        int result = Dice.roll(roll);
        Assert.assertTrue("result should be greater than 4: " + result,
            result > 4);
        Assert.assertTrue("result should be less than 53: " + result,
            result < 53);
    }
}
