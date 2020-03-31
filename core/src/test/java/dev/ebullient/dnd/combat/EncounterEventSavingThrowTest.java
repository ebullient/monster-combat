/*
 * Copyright © 2020 IBM Corp. All rights reserved.
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

import dev.ebullient.dnd.bestiary.MockBeast;
import dev.ebullient.dnd.mechanics.Ability;
import dev.ebullient.dnd.mechanics.Dice;

public class EncounterEventSavingThrowTest {

    @Test
    public void testMakeActionWithSuccessfulSavingThrow() {
        MockBeast targetBeast = new MockBeast("1");
        targetBeast.saveThrows.set(Ability.CON, 3);

        EncounterCombatant actor = new EncounterCombatant(new MockBeast("0"), 10, 10);
        EncounterCombatant target = new EncounterCombatant(targetBeast, 10, 10);

        // force 10 + modifier of 3 (above), which is > 12, which would pass
        target.condition = new MockCondition(Dice.Constraint.TEN);

        MockAttack attack = new MockAttack("testMakeActionWithSuccessfulSavingThrow");
        attack.savingThrow = "CON(12)";

        // an extra effect saving throw shouldn't be used. Make sure if it accentally was, it would fail.
        MockDamage damage = new MockDamage("poison", "14(2d8+5)");
        damage.savingThrow = "INT(12)";
        attack.damage = damage;

        EncounterAttackEvent result = new EncounterAttackEvent(actor, target,
                attack, Dice.Method.USE_AVERAGE, "id");

        result.makeActionWithSavingThrow(attack.damage, false);
        Assert.assertTrue("Saving throw should be successful (throw of 10+3 against 12 DC)", result.saved);
        Assert.assertEquals("Damage amount should be half the average", 7, result.damageAmount);
        Assert.assertFalse("Never critical", result.critical);

        Assert.assertEquals("Attack modifier should be 3", 3, result.getAttackModifier());
        Assert.assertEquals("Difficulty class should be should be 12", 12, result.getDifficultyClass());

        Assert.assertFalse("Extra Effect saving throw should remain false", result.effectSaved);
        Assert.assertEquals("Extra Effect damage amount should remain 0", 0, result.effectAmount);

        target.condition = new MockCondition(Dice.Constraint.CRITICAL);
        result.makeActionWithSavingThrow(attack.damage, false);
        Assert.assertFalse("Never critical", result.critical);
    }

    @Test
    public void testMakeActionWithUnsuccessfulSavingThrow() {
        MockBeast targetBeast = new MockBeast("1");

        EncounterCombatant actor = new EncounterCombatant(new MockBeast("0"), 10, 10);
        EncounterCombatant target = new EncounterCombatant(targetBeast, 10, 10);

        // force 10. No modifier, which is < 12, which should fail
        target.condition = new MockCondition(Dice.Constraint.TEN);

        MockAttack attack = new MockAttack("testMakeActionWithSuccessfulSavingThrow");
        attack.savingThrow = "CON(12)";

        // an extra effect saving throw shouldn't be used. Make sure if it accentally was, it would pass.
        MockDamage damage = new MockDamage("poison", "14(2d8+5)");
        damage.savingThrow = "INT(9)";
        attack.damage = damage;

        EncounterAttackEvent result = new EncounterAttackEvent(actor, target,
                attack, Dice.Method.USE_AVERAGE, "id");

        result.makeActionWithSavingThrow(attack.damage, false);
        Assert.assertFalse("Saving throw should not be successful for attack", result.saved);
        Assert.assertEquals("Damage amount should be the average", 14, result.damageAmount);

        Assert.assertEquals("Attack modifier should be 0", 0, result.getAttackModifier());
        Assert.assertEquals("Difficulty class should be should be 12", 12, result.getDifficultyClass());

        Assert.assertFalse("Extra Effect saving throw should remain false", result.effectSaved);
        Assert.assertEquals("Extra Effect damage amount should remain 0", 0, result.effectAmount);

        target.condition = new MockCondition(Dice.Constraint.FAIL);
        result.makeActionWithSavingThrow(attack.damage, false);
        Assert.assertFalse("Never critical", result.critical);
    }

    @Test
    public void testExtraEffectWithSuccessfulSavingThrow() {
        MockBeast targetBeast = new MockBeast("1");
        targetBeast.saveThrows.set(Ability.INT, 3);

        EncounterCombatant actor = new EncounterCombatant(new MockBeast("0"), 10, 10);
        EncounterCombatant target = new EncounterCombatant(targetBeast, 10, 10);

        // force 10 + modifier of 3 (above), which is > 12, which would pass
        target.condition = new MockCondition(Dice.Constraint.TEN);

        MockAttack attack = new MockAttack("testExtraEffectWithSuccessfulSavingThrow");
        // attack saving throw shouldn't be used. Make sure if it accentally was, it would fail.
        attack.savingThrow = "CON(12)";

        MockDamage damage = new MockDamage("poison", "14(2d8+5)");
        damage.savingThrow = "INT(12)";
        attack.damage = damage;

        EncounterAttackEvent result = new EncounterAttackEvent(actor, target,
                attack, Dice.Method.USE_AVERAGE, "id");

        result.makeActionWithSavingThrow(attack.damage, true);
        Assert.assertFalse("main attack result should remain false", result.saved);
        Assert.assertEquals("main attack damage should remain 0", 0, result.damageAmount);

        Assert.assertTrue("Saving throw should be successful (throw of 10+3 against 12 DC)", result.effectSaved);
        Assert.assertEquals("Damage amount should be half the average", 7, result.effectAmount);
    }

    @Test
    public void testExtraEffectWithUnsuccessfulSavingThrow() {
        MockBeast targetBeast = new MockBeast("1");

        EncounterCombatant actor = new EncounterCombatant(new MockBeast("0"), 10, 10);
        EncounterCombatant target = new EncounterCombatant(targetBeast, 10, 10);

        // force 10. No modifier, which is < 12, which should fail
        target.condition = new MockCondition(Dice.Constraint.TEN);

        MockAttack attack = new MockAttack("testExtraEffectWithUnsuccessfulSavingThrow");
        // attack saving throw shouldn't be used. Make sure if it accentally was, it would pass.
        attack.savingThrow = "CON(9)";

        MockDamage damage = new MockDamage("poison", "14(2d8+5)");
        damage.savingThrow = "INT(12)";
        attack.damage = damage;

        EncounterAttackEvent result = new EncounterAttackEvent(actor, target,
                attack, Dice.Method.USE_AVERAGE, "id");

        result.makeActionWithSavingThrow(attack.damage, true);
        Assert.assertFalse("main attack result should remain false", result.saved);
        Assert.assertEquals("main attack damage should remain 0", 0, result.damageAmount);

        Assert.assertFalse("Saving throw should not be successful for attack", result.effectSaved);
        Assert.assertEquals("Damage amount should be the average", 14, result.effectAmount);
    }
}
