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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import dev.ebullient.dnd.MockAttack;
import dev.ebullient.dnd.MockBeast;
import dev.ebullient.dnd.MockDamage;
import dev.ebullient.dnd.combat.Encounter.RoundResult;
import dev.ebullient.dnd.mechanics.Ability;
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

        mbs[0].size = Size.TINY;
        mbs[0].type = Type.ABERRATION;
        mbs[0].cr = 0; // CR 1/2

        mbs[1].size = Size.TINY;
        mbs[1].type = Type.ABERRATION;
        mbs[1].cr = 0; // CR 1/2

        Set<Combatant> set = new HashSet<>();
        for (int i = 0; i < mbs.length; i++) {
            set.add(new Combatant(mbs[i], 10, 10));
        }

        Encounter e = new Encounter(set, TargetSelector.SelectAtRandom, Dice.Method.USE_AVERAGE);
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

        e = new Encounter(set, TargetSelector.SelectAtRandom, Dice.Method.USE_AVERAGE);
        Assert.assertEquals("Size delta should be 1 when beasts are 1 size apart",
                1, e.sizeDelta);
        Assert.assertEquals("CR delta should be 1 when cr differs by 1",
                1, e.crDelta);
        Assert.assertEquals("Number of types should be 2",
                2, e.numTypes);

        mbs[0].cr = -3; // CR 0
        mbs[1].size = Size.GARGANTUAN;
        mbs[1].cr = 1;

        e = new Encounter(set, TargetSelector.SelectAtRandom, Dice.Method.USE_AVERAGE);
        Assert.assertEquals("Size delta should be 5 (max delta)",
                Size.GARGANTUAN.ordinal() - Size.TINY.ordinal(), e.sizeDelta);
        Assert.assertEquals("CR delta should be 4 when cr ( -3 to 1 )",
                4, e.crDelta);
    }

    @Test
    public void testSingleMeleeAttack() {
        MockBeast[] mbs = new MockBeast[] {
                new MockBeast("0"),
                new MockBeast("1")
        };

        Combatant[] mcs = new Combatant[mbs.length];
        for (int i = 0; i < mbs.length; i++) {
            mcs[i] = new Combatant(mbs[i], 10, 10);
        }

        MockAttack attack = new MockAttack("testMeleeAttack");
        attack.attackModifier = 2;
        attack.damage = new MockDamage("bludgeoning", "14(2d8+5)");

        Encounter.AttackResult result = new Encounter.AttackResult(mcs[0], mcs[1], attack, Dice.Method.USE_AVERAGE, "id");
        result.attack();

        System.out.println(result);
        if (result.hit) {
            Assert.assertFalse("Combatant should be dead (14 damage vs. 10 hit points)", mcs[1].isAlive());
            Assert.assertEquals(14, result.damage);
        } else {
            Assert.assertTrue("Combatant should be alive (miss)", mcs[1].isAlive());
        }
    }

    @Test
    public void testSingleDCAttack() {
        MockBeast[] mbs = new MockBeast[] {
                new MockBeast("0"),
                new MockBeast("1")
        };

        Combatant[] mcs = new Combatant[mbs.length];
        for (int i = 0; i < mbs.length; i++) {
            mcs[i] = new Combatant(mbs[i], 10, 10);
        }

        MockAttack attack = new MockAttack("testDCAttack");
        attack.savingThrow = "CON(22)";
        attack.damage = new MockDamage("poison", "14(2d8+5)");

        Encounter.AttackResult result = new Encounter.AttackResult(mcs[0], mcs[1], attack, Dice.Method.USE_AVERAGE, "id");
        result.attack();

        System.out.println(result);
        if (result.hit) {
            Assert.assertFalse("Combatant should be dead (14 damage vs. 10 hit points)", mcs[1].isAlive());
            Assert.assertEquals(14, result.damage);
        } else {
            Assert.assertTrue("Combatant should be alive (miss)", mcs[1].isAlive());
        }
    }

    @Test
    public void testSingleDCAttackDisadvantage() {
        MockBeast[] mbs = new MockBeast[] {
                new MockBeast("0"),
                new MockBeast("1")
        };

        Combatant[] mcs = new Combatant[] {
                new Combatant(mbs[0], 10, 10),
                new Combatant(mbs[1], 10, 10)
        };

        MockAttack attack = new MockAttack("testDCAttack");
        attack.savingThrow = "CON(22)";
        attack.damage = new MockDamage("poison", "14(2d8+5)");

        mcs[1].addCondition().disadvantage(Arrays.asList(Ability.CON));

        Encounter.AttackResult result = new Encounter.AttackResult(mcs[0], mcs[1], attack, Dice.Method.USE_AVERAGE, "id");
        result.attack();

        System.out.println(result);
        if (result.hit) {
            Assert.assertFalse("Combatant should be dead (14 damage vs. 10 hit points)", mcs[1].isAlive());
            Assert.assertEquals(14, result.damage);
        } else {
            Assert.assertTrue("Combatant should be alive (miss)", mcs[1].isAlive());
        }
    }

    @Test
    public void testRound() {
        MockBeast[] mbs = new MockBeast[] {
                new MockBeast("0"),
                new MockBeast("1")
        };

        mbs[0].size = Size.TINY;
        mbs[0].type = Type.HUMANOID;
        mbs[0].cr = -2; // CR 1/4

        mbs[1].size = Size.TINY;
        mbs[1].type = Type.HUMANOID;
        mbs[1].cr = -2; // CR 1/4

        // Both monsters have the same attack. A DC attack
        // with a save of 30 means it will always hit for full
        // damage (10)
        MockAttack attack = new MockAttack("testDCAttack");
        attack.savingThrow = "CON(30)";
        attack.damage = new MockDamage("poison", "10");

        mbs[0].attacks = Arrays.asList(attack);
        mbs[1].attacks = Arrays.asList(attack);

        // Create list for return result
        List<Combatant> survivors = new ArrayList<>();
        for (int i = 0; i < mbs.length; i++) {
            survivors.add(new Combatant(mbs[i], 10, 30));
        }
        Combatant expected = survivors.get(0);

        Set<Combatant> initiativeOrder = new TreeSet<>(Comparators.InitiativeOrder);
        initiativeOrder.addAll(survivors);

        Encounter r = new Encounter(initiativeOrder, TargetSelector.SelectAtRandom, Dice.Method.USE_AVERAGE);
        Assert.assertFalse(r.isFinal());

        int i = 0;
        while (survivors.size() > 1) {
            i++;
            RoundResult result = r.oneRound();
            survivors = result.getSurvivors();
            Assert.assertEquals(
                    "Encounter should be final when only one survivor remains",
                    survivors.size() == 1, r.isFinal());
            Assert.assertTrue("Should not get to 6 rounds", i <= 6);
        }
        Assert.assertEquals("Should take 3 rounds", 3, i);
        Assert.assertTrue("Combatant 0 should survive: " + survivors, survivors.contains(expected));
    }

    @Test
    public void testExtraEffectRound() {
        MockBeast[] mbs = new MockBeast[] {
                new MockBeast("0"),
                new MockBeast("1")
        };

        Combatant[] mcs = new Combatant[mbs.length];
        for (int i = 0; i < mbs.length; i++) {
            mcs[i] = new Combatant(mbs[i], 10, 10);
        }

        MockDamage effect = new MockDamage("bludgeoning", "2");
        effect = new MockDamage("hpdrain", "");
        effect.savingThrow = "CON(30)";

        MockAttack attack = new MockAttack("testMeleeAttack");
        attack.attackModifier = 2;
        attack.additionalEffect = effect;

        Encounter.AttackResult result = new Encounter.AttackResult(mcs[0], mcs[1], attack, Dice.Method.USE_AVERAGE, "id");
        result.additionalEffects();

        System.out.println(result);
        if (result.hit) {
            Assert.assertFalse("Combatant should be dead (14 damage vs. 10 hit points)", mcs[1].isAlive());
            Assert.assertEquals(14, result.damage);
        } else {
            Assert.assertTrue("Combatant should be alive (miss)", mcs[1].isAlive());
        }

    }
}
