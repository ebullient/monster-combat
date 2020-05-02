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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import dev.ebullient.dnd.bestiary.MockBeast;
import dev.ebullient.dnd.mechanics.Ability;
import dev.ebullient.dnd.mechanics.Dice;

public class EncounterEventConditionTest {

    @Test
    public void testApplyConditionWithDamage() {
        EncounterCombatant[] mcs = new EncounterCombatant[] {
                new EncounterCombatant(new MockBeast("0"), 10, 10),
                new EncounterCombatant(new MockBeast("1"), 10, 10)
        };

        EncounterAttackEvent result = new EncounterAttackEvent(mcs[0], mcs[1],
                new MockAttack("testApplyConditionWithDamage"), Dice.Method.USE_AVERAGE, "id");

        MockDamage damage;
        int damageAmount = 0;

        damage = new MockDamage("bludgeoning", "14(2d8+5)");
        damageAmount = result.applyConditions(damage);
        Assertions.assertEquals(14, damageAmount, "Damage amount should be average");

        Assertions.assertNull(mcs[1].condition, "Condition should be null");
    }

    @Test
    public void testBlindedCondition() {
        EncounterCombatant[] mcs = new EncounterCombatant[] {
                new EncounterCombatant(new MockBeast("0"), 10, 10),
                new EncounterCombatant(new MockBeast("1"), 10, 10)
        };

        EncounterAttackEvent result = new EncounterAttackEvent(mcs[0], mcs[1],
                new MockAttack("testBlindedCondition"), Dice.Method.USE_AVERAGE, "id");

        MockDamage damage = new MockDamage("blinded", "14(2d8+5)");

        int damageAmount = result.applyConditions(damage);
        Assertions.assertEquals(0, damageAmount,
                "Damage amount should be 0 when condition is present");
        Assertions.assertNotNull(mcs[1].condition, "Condition should not be null");

        Assertions.assertEquals(Dice.Constraint.ADVANTAGE, mcs[1].condition.asTarget,
                "blinded condition allows opponent to roll with advantage");
        Assertions.assertEquals(Dice.Constraint.DISADVANTAGE, mcs[1].condition.onAttack,
                "blinded condition forces attack roll with disadvantage");
        Assertions.assertFalse(mcs[1].condition.singleAttack,
                "blinded condition does not constrain the number of attacks");

        Assertions.assertEquals(0, mcs[1].condition.disadvantage.size(),
                "blinded condition does not cause disadvantage on ability checks");
    }

    @Test
    public void testCursedCondition() {
        EncounterCombatant[] mcs = new EncounterCombatant[] {
                new EncounterCombatant(new MockBeast("0"), 10, 10),
                new EncounterCombatant(new MockBeast("1"), 10, 10)
        };

        EncounterAttackEvent result = new EncounterAttackEvent(mcs[0], mcs[1],
                new MockAttack("testCursedCondition"), Dice.Method.USE_AVERAGE, "id");

        MockDamage damage = new MockDamage("cursed", "");
        damage.disadvantage = Arrays.asList(Ability.WIS);

        int damageAmount = result.applyConditions(damage);
        Assertions.assertEquals(0, damageAmount,
                "Damage amount should be 0 when condition is present");
        Assertions.assertNotNull(mcs[1].condition, "Condition should not be null");

        Assertions.assertEquals(Dice.Constraint.NONE, mcs[1].condition.asTarget,
                "cursed condition does not allow opponent to roll with advantage");
        Assertions.assertEquals(Dice.Constraint.NONE, mcs[1].condition.onAttack,
                "cursed condition does not force attack roll with disadvantage");
        Assertions.assertFalse(mcs[1].condition.singleAttack,
                "cursed condition does not constrain the number of attacks");

        Assertions.assertEquals(damage.disadvantage.size(), mcs[1].condition.disadvantage.size(),
                "cursed condition does not cause disadvantage on ability specified in damage");
        Assertions.assertTrue(mcs[1].condition.disadvantage.contains(Ability.WIS),
                "cursed imposes disadvantage on WIS ability checks");
    }

    @Test
    public void testPoisonedCondition() {
        EncounterCombatant[] mcs = new EncounterCombatant[] {
                new EncounterCombatant(new MockBeast("0"), 10, 10),
                new EncounterCombatant(new MockBeast("1"), 10, 10)
        };

        EncounterAttackEvent result = new EncounterAttackEvent(mcs[0], mcs[1],
                new MockAttack("testPoisonedCondition"), Dice.Method.USE_AVERAGE, "id");

        MockDamage damage = new MockDamage("poisoned", "14(2d8+5)");

        int damageAmount = result.applyConditions(damage);
        Assertions.assertEquals(0, damageAmount,
                "Damage amount should be 0 when condition is present");
        Assertions.assertNotNull(mcs[1].condition, "Condition should not be null");

        Assertions.assertEquals(Dice.Constraint.NONE, mcs[1].condition.asTarget,
                "poisoned condition does not allow opponent to roll with advantage");
        Assertions.assertEquals(Dice.Constraint.DISADVANTAGE, mcs[1].condition.onAttack,
                "poisoned condition forces attack roll with disadvantage");
        Assertions.assertFalse(mcs[1].condition.singleAttack,
                "poisoned does not constrain the number of attacks");

        Assertions.assertEquals(6, mcs[1].condition.disadvantage.size(),
                "poisoned imposes disadvantage on all ability checks");
    }

    @Test
    public void testParalyzedCondition() {
        EncounterCombatant[] mcs = new EncounterCombatant[] {
                new EncounterCombatant(new MockBeast("0"), 10, 10),
                new EncounterCombatant(new MockBeast("1"), 10, 10)
        };

        EncounterAttackEvent result = new EncounterAttackEvent(mcs[0], mcs[1],
                new MockAttack("testParalyzedCondition"), Dice.Method.USE_AVERAGE, "id");

        MockDamage damage = new MockDamage("paralyzed", "14(2d8+5)");

        int damageAmount = result.applyConditions(damage);
        Assertions.assertEquals(0, damageAmount,
                "Damage amount should be 0 when condition is present");
        Assertions.assertNotNull(mcs[1].condition, "Condition should not be null");

        Assertions.assertEquals(Dice.Constraint.ADVANTAGE, mcs[1].condition.asTarget,
                "paralyzed condition allows opponent to roll with advantage");
        Assertions.assertEquals(Dice.Constraint.FAIL, mcs[1].condition.onAttack,
                "paralyzed condition fails attack rolls");
        Assertions.assertFalse(mcs[1].condition.singleAttack,
                "paralyzed does not constrain the number of attacks");

        Assertions.assertEquals(2, mcs[1].condition.disadvantage.size(),
                "paralyzed imposes disadvantage on DEX and STR ability checks");
        Assertions.assertTrue(mcs[1].condition.disadvantage.contains(Ability.DEX),
                "paralyzed imposes disadvantage on DEX ability checks");
        Assertions.assertTrue(mcs[1].condition.disadvantage.contains(Ability.STR),
                "paralyzed imposes disadvantage on STR ability checks");
    }

    @Test
    public void testSlowedCondition() {
        EncounterCombatant[] mcs = new EncounterCombatant[] {
                new EncounterCombatant(new MockBeast("0"), 10, 10),
                new EncounterCombatant(new MockBeast("1"), 10, 10)
        };

        EncounterAttackEvent result = new EncounterAttackEvent(mcs[0], mcs[1],
                new MockAttack("testSlowedCondition"), Dice.Method.USE_AVERAGE, "id");

        MockDamage damage = new MockDamage("slowed", "14(2d8+5)");

        int damageAmount = result.applyConditions(damage);
        Assertions.assertEquals(0, damageAmount,
                "Damage amount should be 0 when condition is present");
        Assertions.assertNotNull(mcs[1].condition, "Condition should not be null");

        Assertions.assertEquals(Dice.Constraint.NONE, mcs[1].condition.asTarget,
                "slowed condition does not allow opponent to roll with advantage");
        Assertions.assertEquals(Dice.Constraint.NONE, mcs[1].condition.onAttack,
                "slowed condition doen not force attack roll with disadvantage");
        Assertions.assertTrue(mcs[1].condition.singleAttack,
                "slowed constrains the number of attacks");

        Assertions.assertEquals(0, mcs[1].condition.disadvantage.size(),
                "slowed condition does not cause disadvantage on ability checks");
    }

    @Test
    public void testHPDrainCondition() {
        EncounterCombatant[] mcs = new EncounterCombatant[] {
                new EncounterCombatant(new MockBeast("0"), 10, 10),
                new EncounterCombatant(new MockBeast("1"), 10, 10)
        };

        EncounterAttackEvent result = new EncounterAttackEvent(mcs[0], mcs[1],
                new MockAttack("testHPDrainCondition"), Dice.Method.USE_AVERAGE, "id");
        result.damageAmount = 5;

        MockDamage damage = new MockDamage("hpdrain", "");

        int damageAmount = result.applyConditions(damage);
        Assertions.assertEquals(0, damageAmount,
                "Damage amount should be 0 when condition is present");
        Assertions.assertNotNull(mcs[1].condition,
                "Condition should not be null");

        Assertions.assertEquals(Dice.Constraint.NONE, mcs[1].condition.asTarget,
                "hpdrain condition does not allow opponent to roll with advantage");
        Assertions.assertEquals(Dice.Constraint.NONE, mcs[1].condition.onAttack,
                "hpdrain condition doen not force attack roll with disadvantage");
        Assertions.assertFalse(mcs[1].condition.singleAttack,
                "hpdrain does not constrain the number of attacks");

        Assertions.assertEquals(0, mcs[1].condition.disadvantage.size(),
                "hpdrain condition does not cause disadvantage on ability checks");

        Assertions.assertEquals(5, mcs[1].getMaxHitPoints(),
                "hpdrain does reduce the maximum hit points by attack damage");
    }

}
