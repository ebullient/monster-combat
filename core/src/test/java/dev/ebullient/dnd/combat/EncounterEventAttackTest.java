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

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.ebullient.dnd.bestiary.MockBeast;
import dev.ebullient.dnd.mechanics.Ability;
import dev.ebullient.dnd.mechanics.Dice;

public class EncounterEventAttackTest {

    MockBeast actorBeast;
    MockBeast targetBeast;

    @BeforeEach
    public void resetBeasts() {
        actorBeast = new MockBeast("0");
        targetBeast = new MockBeast("1");
    }

    @Test
    public void testSuccessfulMeleeAttack() {
        targetBeast.armorClass = 11;

        EncounterCombatant actor = new EncounterCombatant(actorBeast, 10, 10);
        EncounterCombatant target = new EncounterCombatant(targetBeast, 10, 10);

        // force roll of 10
        actor.condition = new MockCondition(Dice.Constraint.TEN);

        MockAttack attack = new MockAttack("testSuccessfulMeleeAttack")
                .setAttackModifier(1)
                .setDamage(new MockDamage("piercing", "16(5d6)"));

        EncounterAttackEvent result = new EncounterAttackEvent(actor, target,
                attack, Dice.Method.USE_AVERAGE, "id");

        List<EncounterAttackEvent> list = result.attack();
        Assertions.assertEquals(1, list.size());
        Assertions.assertSame(result, list.get(0));

        Assertions.assertEquals("attack-ac", result.getAttackType(), "AttackType should be attack-ac");

        Assertions.assertTrue(result.hit, "Attack should hit");
        Assertions.assertFalse(result.saved, "Attack was not saved");
        Assertions.assertFalse(result.critical, "Not a critical roll");

        Assertions.assertEquals(16, result.damageAmount, "Damage should be average amount");

        Assertions.assertEquals(1, result.getAttackModifier(), "Attack modifier should be one");
        Assertions.assertEquals(11, result.getDifficultyClass(), "Difficulty class should be should be 11");

        // force roll of 20
        actor.condition = new MockCondition(Dice.Constraint.CRITICAL);
        result.attemptMeleeAttack();

        Assertions.assertEquals(32, result.damageAmount, "Damage should be double average amount");
        Assertions.assertTrue(result.critical, "Critical hit");
    }

    @Test
    public void testUnsuccessfulMeleeAttack() {
        targetBeast.armorClass = 15;

        EncounterCombatant actor = new EncounterCombatant(actorBeast, 10, 10);
        EncounterCombatant target = new EncounterCombatant(targetBeast, 10, 10);

        // force roll of 10
        actor.condition = new MockCondition(Dice.Constraint.TEN);

        // this has an extra effect: because this hit will fail,
        // the extra effect will not be used (result list size of 1)
        MockAttack attack = new MockAttack("testUnsuccessfulMeleeAttack")
                .setAttackModifier(1)
                .setDamage(new MockDamage("piercing", "16(5d6)"))
                .setAdditionalEffect(new MockDamage("poison", "14(2d8+5)")
                        .setSavingThrow("INT(8)"));
        ;

        EncounterAttackEvent result = new EncounterAttackEvent(actor, target,
                attack, Dice.Method.USE_AVERAGE, "id");

        List<EncounterAttackEvent> list = result.attack();
        Assertions.assertEquals(1, list.size());
        Assertions.assertSame(result, list.get(0));

        Assertions.assertEquals("attack-ac", result.getAttackType(), "AttackType should be attack-ac");

        Assertions.assertFalse(result.hit, "Attack should not hit");
        Assertions.assertFalse(result.saved, "Attack was not saved");
        Assertions.assertFalse(result.critical, "Not a critical roll");

        Assertions.assertEquals(0, result.damageAmount, "Damage should be 0");

        Assertions.assertEquals(1, result.getAttackModifier(), "Attack modifier should be one");
        Assertions.assertEquals(15, result.getDifficultyClass(), "Difficulty class should be should be 15");

        // force roll of 1
        actor.condition = new MockCondition(Dice.Constraint.FAIL);
        result.attemptMeleeAttack();

        Assertions.assertTrue(result.critical, "Critical miss");
    }

    @Test
    public void testMakeActionWithSuccessfulSavingThrow() {
        targetBeast.saveThrows.set(Ability.CON, 3);

        EncounterCombatant actor = new EncounterCombatant(actorBeast, 10, 10);
        EncounterCombatant target = new EncounterCombatant(targetBeast, 10, 10);

        // force 10 + modifier of 3 (above), which is > 12, which would pass
        target.condition = new MockCondition(Dice.Constraint.TEN);

        MockAttack attack = new MockAttack("testMakeActionWithSuccessfulSavingThrow")
                .setSavingThrow("CON(12)")
                .setDamage(new MockDamage("poison", "14(2d8+5)"));

        EncounterAttackEvent result = new EncounterAttackEvent(actor, target,
                attack, Dice.Method.USE_AVERAGE, "id");

        List<EncounterAttackEvent> list = result.attack();
        Assertions.assertEquals(1, list.size());
        Assertions.assertSame(result, list.get(0));

        Assertions.assertEquals("attack-dc", result.getAttackType(), "AttackType should be attack-dc");

        Assertions.assertTrue(result.isHit(), "attacks using saving throws always hit");
        Assertions.assertTrue(result.saved, "Saving throw should be successful (throw of 10+3 against 12 DC)");
        Assertions.assertFalse(result.critical, "Not a critical roll");

        Assertions.assertEquals(7, result.damageAmount, "Damage amount should be half the roll average");

        Assertions.assertEquals(3, result.getAttackModifier(), "Attack modifier should be 3");
        Assertions.assertEquals(12, result.getDifficultyClass(), "Difficulty class should be should be 12");

        target.condition = new MockCondition(Dice.Constraint.CRITICAL);
        result.makeActionWithSavingThrow(attack.getSavingThrow(), attack.damage);
        Assertions.assertFalse(result.critical, "Never critical");
    }

    @Test
    public void testMakeActionWithUnsuccessfulSavingThrow() {
        EncounterCombatant actor = new EncounterCombatant(actorBeast, 10, 10);
        EncounterCombatant target = new EncounterCombatant(targetBeast, 10, 10);

        // force 10. No modifier, which is < 12, which should fail
        target.condition = new MockCondition(Dice.Constraint.TEN);

        MockAttack attack = new MockAttack("testMakeActionWithUnsuccessfulSavingThrow")
                .setSavingThrow("CON(12)")
                .setDamage(new MockDamage("poison", "14(2d8+5)"));

        EncounterAttackEvent result = new EncounterAttackEvent(actor, target,
                attack, Dice.Method.USE_AVERAGE, "id");

        List<EncounterAttackEvent> list = result.attack();
        Assertions.assertEquals(1, list.size());
        Assertions.assertSame(result, list.get(0));

        Assertions.assertEquals("attack-dc", result.getAttackType(), "AttackType should be attack-dc");

        Assertions.assertTrue(result.isHit(), "Attacks with a saving throw always hit");
        Assertions.assertFalse(result.saved, "Saving throw should not be successful for attack");

        Assertions.assertEquals(14, result.damageAmount, "Damage amount should be roll average (14)");

        Assertions.assertEquals(0, result.getAttackModifier(), "Attack modifier should be 0");
        Assertions.assertEquals(12, result.getDifficultyClass(), "Difficulty class should be should be 12");

        target.condition = new MockCondition(Dice.Constraint.FAIL);
        result.makeActionWithSavingThrow(attack.getSavingThrow(), attack.damage);
        Assertions.assertFalse(result.critical, "Never critical");
    }

    @Test
    public void testSuccessfulMeleeAttackWithExtraEffect() {
        targetBeast.armorClass = 11;

        EncounterCombatant actor = new EncounterCombatant(actorBeast, 10, 10);
        EncounterCombatant target = new EncounterCombatant(targetBeast, 10, 10);

        // force roll of 10
        actor.condition = new MockCondition(Dice.Constraint.TEN);
        target.condition = new MockCondition(Dice.Constraint.TEN);

        MockAttack attack = new MockAttack("testSuccessfulMeleeAttack")
                .setAttackModifier(1) // 10 + 1 will match 11 armor class to hit
                .setDamage(new MockDamage("piercing", "16(5d6)"))
                .setAdditionalEffect(new MockDamage("poison", "14(2d8+5)")
                        .setSavingThrow("INT(8)"));

        EncounterAttackEvent result = new EncounterAttackEvent(actor, target,
                attack, Dice.Method.USE_AVERAGE, "id");

        List<EncounterAttackEvent> list = result.attack();
        Assertions.assertEquals(2, list.size());

        Assertions.assertSame(result, list.get(0));
        Assertions.assertEquals("attack-ac", result.getAttackType(), "AttackType should be attack-ac");

        Assertions.assertTrue(result.hit, "Attack should hit");
        Assertions.assertFalse(result.saved, "Attack was not saved");
        Assertions.assertFalse(result.critical, "Not a critical hit");

        Assertions.assertEquals(16, result.damageAmount, "Damage should be 16");
        Assertions.assertEquals(1, result.getAttackModifier(), "Attack modifier should be one");
        Assertions.assertEquals(11, result.getDifficultyClass(), "Difficulty class should be should be 11");

        // Additional effect / damage

        result = list.get(1);
        Assertions.assertEquals("attack-dc", result.getAttackType(), "AttackType for extra effect should be attack-dc");
        Assertions.assertTrue(result.hit, "Attacks with a DC always hit");
        Assertions.assertTrue(result.saved, "A roll of 10 will beat a DC of 8");

        Assertions.assertEquals(7, result.damageAmount, "Damage amount should be half the roll average");
        Assertions.assertEquals(0, result.getAttackModifier(), "Attack modifier should be 0");
        Assertions.assertEquals(8, result.getDifficultyClass(), "Difficulty class should be should be 8");
    }

}
