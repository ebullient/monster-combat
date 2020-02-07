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

import dev.ebullient.dnd.MockAttack;
import dev.ebullient.dnd.MockCombatant;
import dev.ebullient.dnd.MockDamage;
import dev.ebullient.dnd.mechanics.Dice;

public class RoundTest {

    @Test
    public void testSingleMeleeAttack() {
        MockCombatant[] mcs = new MockCombatant[] {
                new MockCombatant("0", 10),
                new MockCombatant("1", 10),
        };

        MockAttack attack = new MockAttack("testMeleeAttack");
        attack.attackModifier = 2;
        attack.damage = new MockDamage("bludgeoning", "14(2d8+5)");

        Round.AttackResult result = new Round.AttackResult(mcs[0], mcs[1], attack, Dice.Method.USE_AVERAGE);
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
        MockCombatant[] mcs = new MockCombatant[] {
                new MockCombatant("0", 10),
                new MockCombatant("1", 10),
        };

        MockAttack attack = new MockAttack("testDCAttack");
        attack.savingThrow = "CON(22)";
        attack.damage = new MockDamage("poison", "14(2d8+5)");

        Round.AttackResult result = new Round.AttackResult(mcs[0], mcs[1], attack, Dice.Method.USE_AVERAGE);
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
        MockCombatant[] mcs = new MockCombatant[] {
                new MockCombatant("0", 30),
                new MockCombatant("1", 30),
        };

        MockAttack[] mas = new MockAttack[] {
                new MockAttack("testMeleeAttack"),
                new MockAttack("testDCAttack")
        };

        mas[0].attackModifier = 2;
        mas[0].damage = new MockDamage("bludgeoning", "10");

        mas[1].savingThrow = "CON(1)";
        mas[1].damage = new MockDamage("poison", "10");

        mcs[0].attacks = Arrays.asList(mas);
        mcs[1].attacks = Arrays.asList(mas);
    }
}
