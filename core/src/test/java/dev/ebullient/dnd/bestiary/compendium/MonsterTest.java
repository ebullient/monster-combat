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
package dev.ebullient.dnd.bestiary.compendium;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import dev.ebullient.dnd.combat.Attack;
import dev.ebullient.dnd.mechanics.Ability;
import dev.ebullient.dnd.mechanics.Size;
import dev.ebullient.dnd.mechanics.Type;

/**
 * POJO for monsters read from compendium
 */
public class MonsterTest {

    @Test
    public void testBadStatistics() {
        Monster m = new Monster();

        Assertions.assertThrows(IllegalArgumentException.class, () -> m.setStrength("1"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> m.setDexterity("2"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> m.setConstitution("3"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> m.setIntelligence("4"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> m.setWisdom("5"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> m.setCharisma("6"));
    }

    @Test
    public void testOwlbearMonster() throws Exception {
        Map<String, Monster> compendium;

        try (InputStream jsonInput = CompendiumReader.class.getResourceAsStream("/owlbear.json")) {
            compendium = CompendiumReader.mapper.readValue(jsonInput, CompendiumReader.typeRef);
            Assertions.assertEquals(1, compendium.size());
        }

        // snag our single parsed value
        Monster m = compendium.values().iterator().next();
        Assertions.assertNotNull(m);
        Assertions.assertEquals(Type.MONSTROSITY, m.getType());
        Assertions.assertEquals(Size.LARGE, m.getSize());

        // preset to predictable values ahead of participant creation
        m.hitPoints = "40";
        m.savingThrows = "INT(2)";
        m.saveThrows.set(Ability.INT, 2);

        Assertions.assertEquals(13, m.getArmorClass());
        Assertions.assertEquals(13, m.getPassivePerception());

        Assertions.assertEquals(5, m.getAbilityModifier(Ability.STR));
        Assertions.assertEquals(1, m.getAbilityModifier(Ability.DEX));
        Assertions.assertEquals(3, m.getAbilityModifier(Ability.CON));
        Assertions.assertEquals(-4, m.getAbilityModifier(Ability.INT));
        Assertions.assertEquals(1, m.getAbilityModifier(Ability.WIS));
        Assertions.assertEquals(-2, m.getAbilityModifier(Ability.CHA));

        Assertions.assertNotNull(m.getMultiattack());

        List<Attack> attacks = m.getAttacks();
        Assertions.assertNotNull(attacks, "List of attacks should not be null");
        Assertions.assertFalse(attacks.isEmpty(), "List of attacks should not be empty");
        Multiattack multi = m.getMultiattack();
        Assertions.assertNotNull(multi, "Multi-attack should not be null");
        Assertions.assertEquals(1, multi.combinations.size(), "Multi-attack should have one combination");

        Assertions.assertEquals(5, m.getSavingThrow(Ability.STR),
                "Known modifier should be used when no saving throw is specified: " + m);
        Assertions.assertEquals(2, m.getSavingThrow(Ability.INT),
                "Specified saving throw should be used: " + m);
    }

    @Test
    public void testErinyesMonster() throws Exception {
        Map<String, Monster> compendium;

        try (InputStream jsonInput = CompendiumReader.class.getResourceAsStream("/erinyes.json")) {
            compendium = CompendiumReader.mapper.readValue(jsonInput, CompendiumReader.typeRef);
            Assertions.assertEquals(1, compendium.size());
        }

        // snag our single parsed value
        Monster m = compendium.values().iterator().next();
        Assertions.assertNotNull(m);
        Assertions.assertEquals(Type.FIEND, m.getType());
        Assertions.assertEquals(Size.MEDIUM, m.getSize());

        // This should be multiattack w/ melee string to match
        Assertions.assertNotNull(m.multiattack);
        Assertions.assertEquals(1, m.multiattack.combinations.size(), "Has one combined multiattack");
        Assertions.assertTrue(m.multiattack.combinations.get(0).contains("melee"),
                "Multiattack string contains a melee weapon");

        List<Attack> attacks = m.getAttacks();
        Assertions.assertNotNull(attacks, "List of attacks should not be null");
        Assertions.assertFalse(attacks.isEmpty(), "List of attacks should not be empty");
        Assertions.assertFalse(attacks.toString().contains("melee"), "List of attacks should not contain 'melee'");

        Multiattack multi = m.getMultiattack();
        Assertions.assertNotNull(multi, "Multi-attack should not be null");
        Assertions.assertEquals(1, multi.combinations.size(), "Multi-attack should have one combination");
    }

    @Test
    public void testDragonAttacks() throws Exception {
        Map<String, Monster> compendium;

        try (InputStream jsonInput = CompendiumReader.class.getResourceAsStream("/dragon.json")) {
            compendium = CompendiumReader.mapper.readValue(jsonInput, CompendiumReader.typeRef);
            Assertions.assertEquals(1, compendium.size());
        }

        // snag our single parsed value
        Monster m = compendium.values().iterator().next();
        Assertions.assertNotNull(m);
        Assertions.assertEquals(Type.DRAGON, m.getType());
        Assertions.assertEquals(Size.LARGE, m.getSize());

        List<Attack> attacks = m.getAttacks();
        Assertions.assertNotNull(attacks, "List of attacks should not be null");
        Assertions.assertFalse(attacks.isEmpty(), "List of attacks should not be empty");
        Multiattack multi = m.getMultiattack();
        Assertions.assertNotNull(multi, "Multi-attack should not be null");
        Assertions.assertEquals(1, multi.combinations.size(), "Multi-attack should have one combination");

        // this is the lazy way to check this
        Assertions.assertFalse(attacks.toString().contains("null"), "No attack list elements should be null");
    }

    @Test
    public void testLamiaAttacks() throws Exception {
        Map<String, Monster> compendium;

        try (InputStream jsonInput = CompendiumReader.class.getResourceAsStream("/lamia.json")) {
            compendium = CompendiumReader.mapper.readValue(jsonInput, CompendiumReader.typeRef);
            Assertions.assertEquals(1, compendium.size());
        }

        // snag our single parsed value
        Monster m = compendium.values().iterator().next();
        Assertions.assertNotNull(m);
        Assertions.assertEquals(Type.MONSTROSITY, m.getType());
        Assertions.assertEquals(Size.LARGE, m.getSize());

        List<Attack> attacks = m.getAttacks();
        Assertions.assertNotNull(attacks, "List of attacks should not be null");
        Assertions.assertFalse(attacks.isEmpty(), "List of attacks should not be empty");
        Multiattack multi = m.getMultiattack();
        Assertions.assertNotNull(multi, "Multi-attack should not be null");
        Assertions.assertEquals(2, multi.combinations.size(), "Multi-attack should have two combinations");

        MonsterAttack a = m.actions.get("intoxicating-touch");
        Assertions.assertNotNull(a);
        MonsterDamage d = a.getDamage();
        Assertions.assertEquals("cursed", d.type);
        Assertions.assertEquals(1, d.getDisadvantage().size());
        Assertions.assertEquals(Ability.WIS, d.getDisadvantage().get(0));
        Assertions.assertEquals(60, d.duration, "target is cursed for one hour");
    }

    @Test
    public void testWraithAttacks() throws Exception {
        Map<String, Monster> compendium;

        try (InputStream jsonInput = CompendiumReader.class.getResourceAsStream("/wraith.json")) {
            compendium = CompendiumReader.mapper.readValue(jsonInput, CompendiumReader.typeRef);
            Assertions.assertEquals(1, compendium.size());
        }

        // snag our single parsed value
        Monster m = compendium.values().iterator().next();
        Assertions.assertNotNull(m);
        Assertions.assertEquals(Type.UNDEAD, m.getType());
        Assertions.assertEquals(Size.MEDIUM, m.getSize());

        List<Attack> attacks = m.getAttacks();
        Assertions.assertNotNull(attacks);
        Assertions.assertEquals(1, attacks.size());

        MonsterAttack a = m.actions.get("life-drain");
        Assertions.assertNotNull(a, "life-drain should be a known attack");

        MonsterDamage d = a.getDamage();
        Assertions.assertEquals("necrotic", d.type);
        Assertions.assertNotNull(a.getAdditionalEffect());
    }

    @Test
    public void testConditionAttacks() throws Exception {
        Map<String, Monster> compendium;

        try (InputStream jsonInput = CompendiumReader.class.getResourceAsStream("/gibbering-mouther.json")) {
            compendium = CompendiumReader.mapper.readValue(jsonInput, CompendiumReader.typeRef);
            Assertions.assertEquals(1, compendium.size());
        }

        // snag our single parsed value
        Monster m = compendium.values().iterator().next();
        Assertions.assertNotNull(m);
        Assertions.assertEquals(Type.ABERRATION, m.getType());
        Assertions.assertEquals(Size.MEDIUM, m.getSize());

        List<Attack> attacks = m.getAttacks();
        Assertions.assertNotNull(attacks, "List of attacks should not be null");
        Assertions.assertFalse(attacks.isEmpty(), "List of attacks should not be empty");
        Multiattack multi = m.getMultiattack();
        Assertions.assertNotNull(multi, "Multi-attack should not be null");
        Assertions.assertEquals(1, multi.combinations.size(), "Multi-attack should have one combination");

        MonsterAttack a = m.actions.get("blinding-spittle");
        Assertions.assertNotNull(a, "blinding-spittle should be a known action");
        System.out.println(a);

        MonsterDamage d = a.getDamage();
        System.out.println(d);

        Assertions.assertEquals("blinded", d.type);
        Assertions.assertEquals(1, d.duration, "target is blinded for one turn");
    }
}
