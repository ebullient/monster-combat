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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import dev.ebullient.dnd.bestiary.MockBeast;

public class CombatantTest {

    @Test
    public void testHealth() {

        EncounterCombatant c = new EncounterCombatant(new MockBeast("0"), 10, 40);

        // Poke on some participant behavior
        Assertions.assertEquals(100, c.getRelativeHealth());

        // 1/2 health
        int startingHP = c.hitPoints;
        Assertions.assertEquals(40, startingHP, "Preset value should be used for max health");

        int halfDamage = startingHP / 2;
        c.takeDamage(halfDamage);
        Assertions.assertEquals(50, c.getRelativeHealth(),
                "starting=" + startingHP + ", half damage=" + halfDamage + ", expect relative health of 50");

        // 1/4 health
        startingHP = c.hitPoints;
        halfDamage = startingHP / 2;
        c.takeDamage(halfDamage);
        Assertions.assertEquals(25, c.getRelativeHealth(),
                "starting=" + startingHP + ", half damage=" + halfDamage + ", expect relative health of 25");
    }
}
