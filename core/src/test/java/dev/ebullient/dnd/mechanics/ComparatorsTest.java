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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import dev.ebullient.dnd.MockCombatant;
import dev.ebullient.dnd.combat.Combatant;

public class ComparatorsTest {

    @Test
    public void testInitiativeOrder() {

        MockCombatant[] mcs = new MockCombatant[] {
                new MockCombatant("0", 10),
                new MockCombatant("1", 10),
                new MockCombatant("2", 10),
                new MockCombatant("3", 10)
        };

        List<MockCombatant> combatants = new ArrayList<>(Arrays.asList(mcs));

        combatants.sort(Comparators.InitiativeOrder);
        Assert.assertEquals("when initiative & dex are equal (0), combatants should be sorted in name order",
                "0123", listToString(combatants));

        mcs[0].modifiers.dexterity = 1;
        mcs[1].modifiers.dexterity = 2;
        mcs[2].modifiers.dexterity = 3;
        mcs[3].modifiers.dexterity = 4;

        combatants.sort(Comparators.InitiativeOrder);
        Assert.assertEquals("When initiative is the same, sort by dexterity",
                "3210", listToString(combatants));

        mcs[0].initiative = 10;
        mcs[1].initiative = 2;
        mcs[2].initiative = 17;
        mcs[3].initiative = 6;

        combatants.sort(Comparators.InitiativeOrder);
        Assert.assertEquals("Combatants should be in unique-initiative order",
                "2031", listToString(combatants));
    }

    @Test
    public void testChallengeRatingOrder() {

        MockCombatant[] mcs = new MockCombatant[] {
                new MockCombatant("0", 10),
                new MockCombatant("1", 10),
                new MockCombatant("2", 10),
                new MockCombatant("3", 10)
        };

        List<MockCombatant> combatants = new ArrayList<>(Arrays.asList(mcs));

        combatants.sort(Comparators.ChallengeRatingOrder);
        Assert.assertEquals("when cr (0) and max health (10) are equal, combatants should be sorted in name order",
                "0123", listToString(combatants));

        mcs[1].resetHealth(20);

        combatants.sort(Comparators.ChallengeRatingOrder);
        Assert.assertEquals("when cr (0), combatants should be sorted by max health, then name",
                "0231", listToString(combatants));

        mcs[0].cr = 5;
        mcs[1].cr = -3;
        mcs[2].cr = -3;
        mcs[3].cr = 5;

        combatants.sort(Comparators.ChallengeRatingOrder);
        Assert.assertEquals("combatants sorted in cr order, then in health order, then in name order",
                "2103", listToString(combatants));
    }

    @Test
    public void testRelativeHealthOrder() {

        MockCombatant[] mcs = new MockCombatant[] {
                new MockCombatant("0", 10),
                new MockCombatant("1", 10),
                new MockCombatant("2", 10),
                new MockCombatant("3", 10)
        };

        List<MockCombatant> combatants = new ArrayList<>(Arrays.asList(mcs));

        combatants.sort(Comparators.RelativeHealthOrder);
        Assert.assertEquals("when relative is equal (0), combatants should be sorted in name order",
                "0123", listToString(combatants));

        mcs[0].takeDamage(3);
        mcs[1].takeDamage(7);

        combatants.sort(Comparators.RelativeHealthOrder);
        Assert.assertEquals("combatants sorted in cr order, then in name order",
                "2301", listToString(combatants));
    }

    String listToString(List<MockCombatant> list) {
        StringBuilder sb = new StringBuilder();
        for (Combatant c : list) {
            sb.append(c.getName());
        }
        return sb.toString();
    }

}
