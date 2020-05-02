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
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.ebullient.dnd.bestiary.MockBeast;
import dev.ebullient.dnd.mechanics.Dice;
import dev.ebullient.dnd.mechanics.Size;
import dev.ebullient.dnd.mechanics.Type;

public class EncounterImplTest {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testConstruction() {
        MockBeast[] mbs = new MockBeast[] { new MockBeast("0"), new MockBeast("1") };
        EncounterCombatant[] mcs = new EncounterCombatant[] { new EncounterCombatant(mbs[0], 10, 10),
                new EncounterCombatant(mbs[1], 10, 10) };

        mbs[0].size = Size.TINY;
        mbs[0].type = Type.ABERRATION;
        mbs[0].cr = 0; // CR 1/2

        mbs[1].size = Size.TINY;
        mbs[1].type = Type.ABERRATION;
        mbs[1].cr = 0; // CR 1/2

        Encounter e = new Encounter(Arrays.asList(mcs), EncounterTargetSelector.SelectAtRandom,
                Dice.Method.USE_AVERAGE);
        Assertions.assertEquals(0, e.sizeDelta, "Size delta should be 0 when beasts are the same size");
        Assertions.assertEquals(0, e.crDelta, "CR delta should be 0 when beasts have the same CR");
        Assertions.assertEquals(1, e.numTypes, "Number of types should be 1 when beasts are the same type");
        Assertions.assertEquals(2, e.numCombatants, "Number of combatants should be 2");

        mbs[1].size = Size.SMALL;
        mbs[1].type = Type.BEAST;
        mbs[1].cr = 1;

        e = new Encounter(Arrays.asList(mcs), EncounterTargetSelector.SelectAtRandom, Dice.Method.USE_AVERAGE);
        Assertions.assertEquals(1, e.sizeDelta, "Size delta should be 1 when beasts are 1 size apart");
        Assertions.assertEquals(1, e.crDelta, "CR delta should be 1 when cr differs by 1");
        Assertions.assertEquals(2, e.numTypes, "Number of types should be 2");

        mbs[0].cr = -3; // CR 0
        mbs[1].size = Size.GARGANTUAN;
        mbs[1].cr = 1;

        e = new Encounter(Arrays.asList(mcs), EncounterTargetSelector.SelectAtRandom, Dice.Method.USE_AVERAGE);
        Assertions.assertEquals(Size.GARGANTUAN.ordinal() - Size.TINY.ordinal(),
                e.sizeDelta, "Size delta should be 5 (max delta)");
        Assertions.assertEquals(4, e.crDelta, "CR delta should be 4 when cr ( -3 to 1 )");
    }

    @Test
    public void testRound() {
        MockBeast[] mbs = new MockBeast[] { new MockBeast("0"), new MockBeast("1") };

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

        List<EncounterCombatant> combatants = Arrays.asList(new EncounterCombatant(mbs[0], 10, 40),
                new EncounterCombatant(mbs[1], 10, 30));

        Encounter r = new Encounter(combatants, EncounterTargetSelector.SelectAtRandom, Dice.Method.USE_AVERAGE);
        Assertions.assertFalse(r.isFinal());

        int i = 0;

        // Create list for return result
        List<? extends Combatant> survivors = combatants;
        while (survivors.size() > 1) {
            i++;
            RoundResult result = r.oneRound();
            survivors = result.getSurvivors();
            Assertions.assertEquals(survivors.size() == 1, r.isFinal(),
                    "Encounter should be final when only one survivor remains");
            Assertions.assertTrue(i <= 5, "Should not get to 5 rounds");
            testSerializeResult(result);
        }

        Assertions.assertEquals("0", survivors.get(0).getName(),
                "Combatant 0 should survive: " + survivors);
    }

    void testSerializeResult(RoundResult result) {
        Assertions.assertDoesNotThrow(() -> {
            mapper.convertValue((EncounterRoundResult) result, dev.ebullient.dnd.combat.client.RoundResult.class);
        });
    }
}
