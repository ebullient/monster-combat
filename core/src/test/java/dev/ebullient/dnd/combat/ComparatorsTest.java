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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import dev.ebullient.dnd.MockBeast;

public class ComparatorsTest {

    @Test
    public void testInitiativeOrder() {

        MockBeast[] mbs = new MockBeast[] {
                new MockBeast("0"),
                new MockBeast("1"),
                new MockBeast("2"),
                new MockBeast("3")
        };

        List<Combatant> combatants = new ArrayList<>(mbs.length);
        for (int i = 0; i < mbs.length; i++) {
            combatants.add(new Combatant(mbs[i], 10, 10));
        }

        combatants.sort(Comparators.InitiativeOrder);

        Assert.assertEquals("when initiative & dex are equal (0), combatants should be sorted in name order",
                "0123", listToString(combatants));

        mbs[0].modifiers.dexterity = 1;
        mbs[1].modifiers.dexterity = 2;
        mbs[2].modifiers.dexterity = 3;
        mbs[3].modifiers.dexterity = 4;

        combatants.sort(Comparators.InitiativeOrder);
        Assert.assertEquals("When initiative is the same, sort by dexterity",
                "3210", listToString(combatants));

        combatants.set(0, new Combatant(mbs[0], 10, 10));
        combatants.set(1, new Combatant(mbs[1], 2, 10));
        combatants.set(2, new Combatant(mbs[2], 17, 10));
        combatants.set(3, new Combatant(mbs[3], 6, 10));

        combatants.sort(Comparators.InitiativeOrder);
        Assert.assertEquals("Combatants should be in unique-initiative order",
                "2031", listToString(combatants));
    }

    @Test
    public void testChallengeRatingOrder() {

        MockBeast[] mbs = new MockBeast[] {
                new MockBeast("0"),
                new MockBeast("1"),
                new MockBeast("2"),
                new MockBeast("3")
        };

        List<Combatant> combatants = new ArrayList<>(mbs.length);
        for (int i = 0; i < mbs.length; i++) {
            combatants.add(new Combatant(mbs[i], 10, 10));
        }

        combatants.sort(Comparators.ChallengeRatingOrder);
        Assert.assertEquals("when cr (0) and max health (10) are equal, combatants should be sorted in name order",
                "0123", listToString(combatants));

        combatants.set(1, new Combatant(mbs[1], 10, 20));

        combatants.sort(Comparators.ChallengeRatingOrder);
        Assert.assertEquals("when cr (0), combatants should be sorted by max health, then name",
                "1023", listToString(combatants));

        mbs[0].cr = 5;
        mbs[1].cr = -3;
        mbs[2].cr = -3;
        mbs[3].cr = 5;

        combatants.sort(Comparators.ChallengeRatingOrder);
        Assert.assertEquals("combatants sorted in cr order, then in health order, then in name order",
                "0312", listToString(combatants));
    }

    @Test
    public void testRelativeHealthOrder() {

        MockBeast[] mbs = new MockBeast[] {
                new MockBeast("0"),
                new MockBeast("1"),
                new MockBeast("2"),
                new MockBeast("3")
        };

        Combatant[] mcs = new Combatant[mbs.length];
        for (int i = 0; i < mbs.length; i++) {
            mcs[i] = new Combatant(mbs[i], 10, 10);
        }

        List<Combatant> combatants = new ArrayList<>(Arrays.asList(mcs));
        combatants.sort(Comparators.RelativeHealthOrder);
        Assert.assertEquals("when relative is equal (0), combatants should be sorted in name order",
                "0123", listToString(combatants));

        mcs[0].takeDamage(3);
        mcs[1].takeDamage(7);

        combatants.sort(Comparators.RelativeHealthOrder);
        Assert.assertEquals("combatants sorted in cr order, then in name order",
                "2301", listToString(combatants));
    }

    String listToString(List<Combatant> list) {
        StringBuilder sb = new StringBuilder();
        for (Combatant c : list) {
            sb.append(c.getName());
        }
        return sb.toString();
    }

}
