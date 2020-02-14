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
import dev.ebullient.dnd.mechanics.Ability;
import dev.ebullient.dnd.mechanics.Dice;

public class EncounterEventConditionTest {

    @Test
    public void testApplyConditionWithDamage() {
        Combatant[] mcs = new Combatant[] {
                new Combatant(new MockBeast("0"), 10, 10),
                new Combatant(new MockBeast("1"), 10, 10)
        };

        Encounter.AttackEvent result = new Encounter.AttackEvent(mcs[0], mcs[1],
                new MockAttack("testApplyConditionWithDamage"), Dice.Method.USE_AVERAGE, "id");

        MockDamage damage;
        int damageAmount = 0;

        damage = new MockDamage("bludgeoning", "14(2d8+5)");
        damageAmount = result.applyConditions(damage);
        Assert.assertEquals("Damage amount should be average",
                14, damageAmount);
        Assert.assertNull("Condition should be null", mcs[1].condition);
    }

    @Test
    public void testBlindedCondition() {
        Combatant[] mcs = new Combatant[] {
                new Combatant(new MockBeast("0"), 10, 10),
                new Combatant(new MockBeast("1"), 10, 10)
        };

        Encounter.AttackEvent result = new Encounter.AttackEvent(mcs[0], mcs[1],
                new MockAttack("testBlindedCondition"), Dice.Method.USE_AVERAGE, "id");

        MockDamage damage = new MockDamage("blinded", "14(2d8+5)");

        int damageAmount = result.applyConditions(damage);
        Assert.assertEquals("Damage amount should be 0 when condition is present",
                0, damageAmount);
        Assert.assertNotNull("Condition should not be null", mcs[1].condition);

        Assert.assertEquals("blinded condition allows opponent to roll with advantage",
                Dice.Constraint.ADVANTAGE, mcs[1].condition.asTarget);
        Assert.assertEquals("blinded condition forces attack roll with disadvantage",
                Dice.Constraint.DISADVANTAGE, mcs[1].condition.onAttack);
        Assert.assertFalse("blinded condition does not constrain the number of attacks",
                mcs[1].condition.singleAttack);

        Assert.assertEquals("blinded condition does not cause disadvantage on ability checks",
                0, mcs[1].condition.disadvantage.size());
    }

    @Test
    public void testCursedCondition() {
        Combatant[] mcs = new Combatant[] {
                new Combatant(new MockBeast("0"), 10, 10),
                new Combatant(new MockBeast("1"), 10, 10)
        };

        Encounter.AttackEvent result = new Encounter.AttackEvent(mcs[0], mcs[1],
                new MockAttack("testCursedCondition"), Dice.Method.USE_AVERAGE, "id");

        MockDamage damage = new MockDamage("cursed", "");
        damage.disadvantage = Arrays.asList(Ability.WIS);

        int damageAmount = result.applyConditions(damage);
        Assert.assertEquals("Damage amount should be 0 when condition is present",
                0, damageAmount);
        Assert.assertNotNull("Condition should not be null", mcs[1].condition);

        Assert.assertEquals("cursed condition does not allow opponent to roll with advantage",
                Dice.Constraint.NONE, mcs[1].condition.asTarget);
        Assert.assertEquals("cursed condition does not force attack roll with disadvantage",
                Dice.Constraint.NONE, mcs[1].condition.onAttack);
        Assert.assertFalse("cursed condition does not constrain the number of attacks",
                mcs[1].condition.singleAttack);

        Assert.assertEquals("cursed condition does not cause disadvantage on ability specified in damage",
                damage.disadvantage.size(), mcs[1].condition.disadvantage.size());
        Assert.assertTrue("cursed imposes disadvantage on WIS ability checks",
                mcs[1].condition.disadvantage.contains(Ability.WIS));
    }

    @Test
    public void testPoisonedCondition() {
        Combatant[] mcs = new Combatant[] {
                new Combatant(new MockBeast("0"), 10, 10),
                new Combatant(new MockBeast("1"), 10, 10)
        };

        Encounter.AttackEvent result = new Encounter.AttackEvent(mcs[0], mcs[1],
                new MockAttack("testPoisonedCondition"), Dice.Method.USE_AVERAGE, "id");

        MockDamage damage = new MockDamage("poisoned", "14(2d8+5)");

        int damageAmount = result.applyConditions(damage);
        Assert.assertEquals("Damage amount should be 0 when condition is present",
                0, damageAmount);
        Assert.assertNotNull("Condition should not be null", mcs[1].condition);

        Assert.assertEquals("poisoned condition does not allow opponent to roll with advantage",
                Dice.Constraint.NONE, mcs[1].condition.asTarget);
        Assert.assertEquals("poisoned condition forces attack roll with disadvantage",
                Dice.Constraint.DISADVANTAGE, mcs[1].condition.onAttack);
        Assert.assertFalse("poisoned does not constrain the number of attacks",
                mcs[1].condition.singleAttack);

        Assert.assertEquals("poisoned imposes disadvantage on all ability checks",
                6, mcs[1].condition.disadvantage.size());
    }

    @Test
    public void testParalyzedCondition() {
        Combatant[] mcs = new Combatant[] {
                new Combatant(new MockBeast("0"), 10, 10),
                new Combatant(new MockBeast("1"), 10, 10)
        };

        Encounter.AttackEvent result = new Encounter.AttackEvent(mcs[0], mcs[1],
                new MockAttack("testParalyzedCondition"), Dice.Method.USE_AVERAGE, "id");

        MockDamage damage = new MockDamage("paralyzed", "14(2d8+5)");

        int damageAmount = result.applyConditions(damage);
        Assert.assertEquals("Damage amount should be 0 when condition is present",
                0, damageAmount);
        Assert.assertNotNull("Condition should not be null", mcs[1].condition);

        Assert.assertEquals("paralyzed condition allows opponent to roll with advantage",
                Dice.Constraint.ADVANTAGE, mcs[1].condition.asTarget);
        Assert.assertEquals("paralyzed condition fails attack rolls",
                Dice.Constraint.FAIL, mcs[1].condition.onAttack);
        Assert.assertFalse("paralyzed does not constrain the number of attacks",
                mcs[1].condition.singleAttack);

        Assert.assertEquals("paralyzed imposes disadvantage on DEX and STR ability checks",
                2, mcs[1].condition.disadvantage.size());
        Assert.assertTrue("paralyzed imposes disadvantage on DEX ability checks",
                mcs[1].condition.disadvantage.contains(Ability.DEX));
        Assert.assertTrue("paralyzed imposes disadvantage on STR ability checks",
                mcs[1].condition.disadvantage.contains(Ability.STR));
    }

    @Test
    public void testSlowedCondition() {
        Combatant[] mcs = new Combatant[] {
                new Combatant(new MockBeast("0"), 10, 10),
                new Combatant(new MockBeast("1"), 10, 10)
        };

        Encounter.AttackEvent result = new Encounter.AttackEvent(mcs[0], mcs[1],
                new MockAttack("testSlowedCondition"), Dice.Method.USE_AVERAGE, "id");

        MockDamage damage = new MockDamage("slowed", "14(2d8+5)");

        int damageAmount = result.applyConditions(damage);
        Assert.assertEquals("Damage amount should be 0 when condition is present",
                0, damageAmount);
        Assert.assertNotNull("Condition should not be null", mcs[1].condition);

        Assert.assertEquals("slowed condition does not allow opponent to roll with advantage",
                Dice.Constraint.NONE, mcs[1].condition.asTarget);
        Assert.assertEquals("slowed condition doen not force attack roll with disadvantage",
                Dice.Constraint.NONE, mcs[1].condition.onAttack);
        Assert.assertTrue("slowed constrains the number of attacks",
                mcs[1].condition.singleAttack);

        Assert.assertEquals("slowed condition does not cause disadvantage on ability checks",
                0, mcs[1].condition.disadvantage.size());
    }

    @Test
    public void testHPDrainCondition() {
        Combatant[] mcs = new Combatant[] {
                new Combatant(new MockBeast("0"), 10, 10),
                new Combatant(new MockBeast("1"), 10, 10)
        };

        Encounter.AttackEvent result = new Encounter.AttackEvent(mcs[0], mcs[1],
                new MockAttack("testHPDrainCondition"), Dice.Method.USE_AVERAGE, "id");
        result.damageAmount = 5;

        MockDamage damage = new MockDamage("hpdrain", "");

        int damageAmount = result.applyConditions(damage);
        Assert.assertEquals("Damage amount should be 0 when condition is present",
                0, damageAmount);
        Assert.assertNotNull("Condition should not be null", mcs[1].condition);

        Assert.assertEquals("hpdrain condition does not allow opponent to roll with advantage",
                Dice.Constraint.NONE, mcs[1].condition.asTarget);
        Assert.assertEquals("hpdrain condition doen not force attack roll with disadvantage",
                Dice.Constraint.NONE, mcs[1].condition.onAttack);
        Assert.assertFalse("hpdrain does not constrain the number of attacks",
                mcs[1].condition.singleAttack);

        Assert.assertEquals("hpdrain condition does not cause disadvantage on ability checks",
                0, mcs[1].condition.disadvantage.size());

        Assert.assertEquals("hpdrain does reduce the maximum hit points by attack damage",
                5, mcs[1].getMaxHitPoints());
    }

}
