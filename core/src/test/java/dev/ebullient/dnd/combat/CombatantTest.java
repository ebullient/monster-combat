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
package dev.ebullient.dnd.combat;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import dev.ebullient.dnd.beastiary.MockBeast;

public class CombatantTest {

    @Test
    public void testHealth() {

        Combatant c = new Combatant(new MockBeast("0"), 10, 40);

        // Poke on some participant behavior
        Assert.assertEquals(100, c.getRelativeHealth());

        // 1/2 health
        int startingHP = c.hitPoints;
        Assert.assertEquals("Preset value for max health", 40, startingHP);

        int halfDamage = startingHP / 2;
        c.takeDamage(halfDamage);
        Assert.assertEquals("starting=" + startingHP + ", half damage=" + halfDamage + ", expect relative health of 50",
                50, c.getRelativeHealth());

        // 1/4 health
        startingHP = c.hitPoints;
        halfDamage = startingHP / 2;
        c.takeDamage(halfDamage);
        Assert.assertEquals("starting=" + startingHP + ", half damage=" + halfDamage + ", expect relative health of 25",
                25, c.getRelativeHealth());
    }
}
