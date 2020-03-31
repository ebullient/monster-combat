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

import dev.ebullient.dnd.bestiary.MockBeast;
import dev.ebullient.dnd.mechanics.Ability;
import dev.ebullient.dnd.mechanics.Size;

public class EncounterComparatorsTest {

    @Test
    public void testInitiativeOrder() {

        MockBeast[] mbs = new MockBeast[] {
                new MockBeast("0"),
                new MockBeast("1"),
                new MockBeast("2"),
                new MockBeast("3")
        };

        List<EncounterCombatant> combatants = new ArrayList<>(mbs.length);
        for (int i = 0; i < mbs.length; i++) {
            combatants.add(new EncounterCombatant(mbs[i], 10, 10));
        }

        combatants.sort(EncounterComparators.InitiativeOrder);

        Assert.assertEquals("when initiative & dex are equal (0), combatants should be sorted in name order",
                "0123", listToString(combatants));

        mbs[0].modifiers.set(Ability.DEX, 1);
        mbs[1].modifiers.set(Ability.DEX, 2);
        mbs[2].modifiers.set(Ability.DEX, 3);
        mbs[3].modifiers.set(Ability.DEX, 4);

        combatants.sort(EncounterComparators.InitiativeOrder);
        Assert.assertEquals("When initiative is the same, sort by dexterity",
                "3210", listToString(combatants));

        combatants.set(0, new EncounterCombatant(mbs[0], 10, 10));
        combatants.set(1, new EncounterCombatant(mbs[1], 2, 10));
        combatants.set(2, new EncounterCombatant(mbs[2], 17, 10));
        combatants.set(3, new EncounterCombatant(mbs[3], 6, 10));

        combatants.sort(EncounterComparators.InitiativeOrder);
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

        List<EncounterCombatant> combatants = new ArrayList<>(mbs.length);
        for (int i = 0; i < mbs.length; i++) {
            combatants.add(new EncounterCombatant(mbs[i], 10, 10));
        }

        combatants.sort(EncounterComparators.ChallengeRatingOrder);
        Assert.assertEquals("when cr (0) and max health (10) are equal, combatants should be sorted in name order",
                "0123", listToString(combatants));

        combatants.set(1, new EncounterCombatant(mbs[1], 10, 20));

        combatants.sort(EncounterComparators.ChallengeRatingOrder);
        Assert.assertEquals("when cr (0), combatants should be sorted by max health, then name",
                "1023", listToString(combatants));

        mbs[0].cr = 5;
        mbs[1].cr = -3;
        mbs[2].cr = -3;
        mbs[3].cr = 5;

        combatants.sort(EncounterComparators.ChallengeRatingOrder);
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

        EncounterCombatant[] mcs = new EncounterCombatant[mbs.length];
        for (int i = 0; i < mbs.length; i++) {
            mcs[i] = new EncounterCombatant(mbs[i], 10, 10);
        }

        List<EncounterCombatant> combatants = new ArrayList<>(Arrays.asList(mcs));
        combatants.sort(EncounterComparators.RelativeHealthOrder);
        Assert.assertEquals("when health is equal (0), combatants should be sorted in name order",
                "0123", listToString(combatants));

        mcs[0].takeDamage(3);
        mcs[1].takeDamage(7);

        combatants.sort(EncounterComparators.RelativeHealthOrder);
        Assert.assertEquals("combatants sorted in health order, then in name order",
                "2301", listToString(combatants));
    }

    @Test
    public void testSizeOrder() {

        MockBeast[] mbs = new MockBeast[] {
                new MockBeast("0"),
                new MockBeast("1"),
                new MockBeast("2"),
                new MockBeast("3")
        };

        EncounterCombatant[] mcs = new EncounterCombatant[mbs.length];
        for (int i = 0; i < mbs.length; i++) {
            mcs[i] = new EncounterCombatant(mbs[i], 10, 10);
        }

        List<EncounterCombatant> combatants = new ArrayList<>(Arrays.asList(mcs));
        combatants.sort(EncounterComparators.RelativeHealthOrder);
        Assert.assertEquals("when all are the same size, combatants should be sorted in name order",
                "0123", listToString(combatants));

        mbs[2].size = Size.GARGANTUAN;
        mbs[3].size = Size.LARGE;

        combatants.sort(EncounterComparators.SizeOrder);
        Assert.assertEquals("combatants sorted in size order, then in name order",
                "2301", listToString(combatants));
    }

    String listToString(List<EncounterCombatant> list) {
        StringBuilder sb = new StringBuilder();
        for (EncounterCombatant c : list) {
            sb.append(c.getName());
        }
        return sb.toString();
    }

}
