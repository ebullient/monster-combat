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

import java.util.Arrays;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import dev.ebullient.dnd.beastiary.MockBeast;
import dev.ebullient.dnd.mechanics.Dice;
import dev.ebullient.dnd.mechanics.Size;
import dev.ebullient.dnd.mechanics.Type;

public class EncounterTest {

    @Test
    public void testConstruction() {
        MockBeast[] mbs = new MockBeast[] {
                new MockBeast("0"),
                new MockBeast("1")
        };
        Combatant[] mcs = new Combatant[] {
                new Combatant(mbs[0], 10, 10),
                new Combatant(mbs[1], 10, 10)
        };

        mbs[0].size = Size.TINY;
        mbs[0].type = Type.ABERRATION;
        mbs[0].cr = 0; // CR 1/2

        mbs[1].size = Size.TINY;
        mbs[1].type = Type.ABERRATION;
        mbs[1].cr = 0; // CR 1/2

        Encounter e = new Encounter(Arrays.asList(mcs), TargetSelector.SelectAtRandom, Dice.Method.USE_AVERAGE);
        Assert.assertEquals("Size delta should be 0 when beasts are the same size",
                0, e.sizeDelta);
        Assert.assertEquals("CR delta should be 0 when beasts have the same CR",
                0, e.crDelta);
        Assert.assertEquals("Number of types should be 1 when beasts are the same type",
                1, e.numTypes);
        Assert.assertEquals("Number of combatants should be 2",
                2, e.numCombatants);

        mbs[1].size = Size.SMALL;
        mbs[1].type = Type.BEAST;
        mbs[1].cr = 1;

        e = new Encounter(Arrays.asList(mcs), TargetSelector.SelectAtRandom, Dice.Method.USE_AVERAGE);
        Assert.assertEquals("Size delta should be 1 when beasts are 1 size apart",
                1, e.sizeDelta);
        Assert.assertEquals("CR delta should be 1 when cr differs by 1",
                1, e.crDelta);
        Assert.assertEquals("Number of types should be 2",
                2, e.numTypes);

        mbs[0].cr = -3; // CR 0
        mbs[1].size = Size.GARGANTUAN;
        mbs[1].cr = 1;

        e = new Encounter(Arrays.asList(mcs), TargetSelector.SelectAtRandom, Dice.Method.USE_AVERAGE);
        Assert.assertEquals("Size delta should be 5 (max delta)",
                Size.GARGANTUAN.ordinal() - Size.TINY.ordinal(), e.sizeDelta);
        Assert.assertEquals("CR delta should be 4 when cr ( -3 to 1 )",
                4, e.crDelta);
    }

}
