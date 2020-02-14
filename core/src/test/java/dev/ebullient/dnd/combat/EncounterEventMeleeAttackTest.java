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

import dev.ebullient.dnd.beastiary.MockBeast;
import dev.ebullient.dnd.mechanics.Dice;

public class EncounterEventMeleeAttackTest {

    @Test
    public void testSuccessfulMeleeAttack() {
        MockBeast targetBeast = new MockBeast("1");
        targetBeast.armorClass = 11;

        Combatant actor = new Combatant(new MockBeast("0"), 10, 10);
        Combatant target = new Combatant(targetBeast, 10, 10);

        // force roll of 10
        actor.condition = new MockCondition(Dice.Constraint.TEN);

        MockAttack attack = new MockAttack("testSuccessfulMeleeAttack");
        attack.attackModifier = 1;
        attack.damage = new MockDamage("piercing", "16(5d6)");

        Encounter.AttackEvent result = new Encounter.AttackEvent(actor, target,
                attack, Dice.Method.USE_AVERAGE, "id");

        result.attemptMeleeAttack();

        Assert.assertTrue("Attack should hit", result.hit);
        Assert.assertEquals("Damage should be average amount", 16, result.damageAmount);
        Assert.assertFalse("Attack was not saved", result.saved);
        Assert.assertFalse("Not a critical hit", result.critical);

        // force roll of 20
        actor.condition = new MockCondition(Dice.Constraint.CRITICAL);
        result.attemptMeleeAttack();

        Assert.assertEquals("Damage should be double average amount", 32, result.damageAmount);
        Assert.assertTrue("Critical hit", result.critical);
    }

    @Test
    public void testUnuccessfulMeleeAttack() {
        MockBeast targetBeast = new MockBeast("1");
        targetBeast.armorClass = 15;

        Combatant actor = new Combatant(new MockBeast("0"), 10, 10);
        Combatant target = new Combatant(targetBeast, 10, 10);

        // force roll of 10
        actor.condition = new MockCondition(Dice.Constraint.TEN);

        MockAttack attack = new MockAttack("testUnuccessfulMeleeAttack");
        attack.attackModifier = 1;
        attack.damage = new MockDamage("piercing", "16(5d6)");

        Encounter.AttackEvent result = new Encounter.AttackEvent(actor, target,
                attack, Dice.Method.USE_AVERAGE, "id");

        result.attemptMeleeAttack();

        Assert.assertFalse("Attack should not hit", result.hit);
        Assert.assertEquals("Damage should be 0", 0, result.damageAmount);
        Assert.assertFalse("Attack was not saved", result.saved);
        Assert.assertFalse("Not a critical miss", result.critical);

        // force roll of 1
        actor.condition = new MockCondition(Dice.Constraint.FAIL);
        result.attemptMeleeAttack();

        Assert.assertTrue("Critical miss", result.critical);
    }
}
