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
package dev.ebullient.dnd.beastiary.compendium;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
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

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            m.setStrength("1");
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            m.setDexterity("2");
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            m.setConstitution("3");
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            m.setIntelligence("4");
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            m.setWisdom("5");
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            m.setCharisma("6");
        });
    }

    @Test
    public void testOwlbearMonster() throws Exception {
        Map<String, Monster> compendium;

        try (InputStream jsonInput = CompendiumReader.class.getResourceAsStream("/owlbear.json")) {
            compendium = CompendiumReader.mapper.readValue(jsonInput, CompendiumReader.typeRef);
            Assert.assertEquals(1, compendium.size());
        }

        // snag our single parsed value
        Monster m = compendium.values().iterator().next();
        Assert.assertNotNull(m);
        Assert.assertEquals(Type.MONSTROSITY, m.getType());
        Assert.assertEquals(Size.LARGE, m.getSize());

        // preset to predictable values ahead of participant creation
        m.hitPoints = "40";
        m.savingThrows = "INT(2)";
        m.saveThrows.set(Ability.INT, 2);

        Assert.assertEquals(13, m.getArmorClass());
        Assert.assertEquals(13, m.getPassivePerception());

        Assert.assertEquals(5, m.getAbilityModifier(Ability.STR));
        Assert.assertEquals(1, m.getAbilityModifier(Ability.DEX));
        Assert.assertEquals(3, m.getAbilityModifier(Ability.CON));
        Assert.assertEquals(-4, m.getAbilityModifier(Ability.INT));
        Assert.assertEquals(1, m.getAbilityModifier(Ability.WIS));
        Assert.assertEquals(-2, m.getAbilityModifier(Ability.CHA));

        Assert.assertNotNull(m.getMultiattack());

        List<Attack> attacks = m.getAttacks();
        Assert.assertNotNull("List of attacks should not be null", attacks);
        Assert.assertFalse("List of attacks should not be empty", attacks.isEmpty());
        Multiattack multi = m.getMultiattack();
        Assert.assertNotNull("Multi-attack should not be null", multi);
        Assert.assertEquals("Multi-attack should have one combination", 1, multi.combinations.size());

        Assert.assertEquals("Known modifier should be used when no saving throw is specified: " + m,
                5, m.getSavingThrow(Ability.STR));
        Assert.assertEquals("Specified saving throw should be used: " + m,
                2, m.getSavingThrow(Ability.INT));
    }

    @Test
    public void testErinyesMonster() throws Exception {
        Map<String, Monster> compendium;

        try (InputStream jsonInput = CompendiumReader.class.getResourceAsStream("/erinyes.json")) {
            compendium = CompendiumReader.mapper.readValue(jsonInput, CompendiumReader.typeRef);
            Assert.assertEquals(1, compendium.size());
        }

        // snag our single parsed value
        Monster m = compendium.values().iterator().next();
        Assert.assertNotNull(m);
        Assert.assertEquals(Type.FIEND, m.getType());
        Assert.assertEquals(Size.MEDIUM, m.getSize());

        // This should be multiattack w/ melee string to match
        Assert.assertNotNull(m.multiattack);
        Assert.assertEquals("Has one combined multiattack", 1, m.multiattack.combinations.size());
        Assert.assertTrue("Multiattack string contains a melee weapon", m.multiattack.combinations.get(0).contains("melee"));

        List<Attack> attacks = m.getAttacks();
        Assert.assertNotNull(attacks);
        Assert.assertNotNull("List of attacks should not be null", attacks);
        Assert.assertFalse("List of attacks should not be empty", attacks.isEmpty());
        Assert.assertFalse("List of attacks should not contain 'melee'", attacks.toString().contains("melee"));

        Multiattack multi = m.getMultiattack();
        Assert.assertNotNull("Multi-attack should not be null", multi);
        Assert.assertEquals("Multi-attack should have one combination", 1, multi.combinations.size());
    }

    @Test
    public void testDragonAttacks() throws Exception {
        Map<String, Monster> compendium;

        try (InputStream jsonInput = CompendiumReader.class.getResourceAsStream("/dragon.json")) {
            compendium = CompendiumReader.mapper.readValue(jsonInput, CompendiumReader.typeRef);
            Assert.assertEquals(1, compendium.size());
        }

        // snag our single parsed value
        Monster m = compendium.values().iterator().next();
        Assert.assertNotNull(m);
        Assert.assertEquals(Type.DRAGON, m.getType());
        Assert.assertEquals(Size.LARGE, m.getSize());

        List<Attack> attacks = m.getAttacks();
        Assert.assertNotNull("List of attacks should not be null", attacks);
        Assert.assertFalse("List of attacks should not be empty", attacks.isEmpty());
        Multiattack multi = m.getMultiattack();
        Assert.assertNotNull("Multi-attack should not be null", multi);
        Assert.assertEquals("Multi-attack should have one combination", 1, multi.combinations.size());

        // this is the lazy way to check this
        Assert.assertFalse("No attack list elements should be null", attacks.toString().contains("null"));
    }

    @Test
    public void testLamiaAttacks() throws Exception {
        Map<String, Monster> compendium;

        try (InputStream jsonInput = CompendiumReader.class.getResourceAsStream("/lamia.json")) {
            compendium = CompendiumReader.mapper.readValue(jsonInput, CompendiumReader.typeRef);
            Assert.assertEquals(1, compendium.size());
        }

        // snag our single parsed value
        Monster m = compendium.values().iterator().next();
        Assert.assertNotNull(m);
        Assert.assertEquals(Type.MONSTROSITY, m.getType());
        Assert.assertEquals(Size.LARGE, m.getSize());

        List<Attack> attacks = m.getAttacks();
        Assert.assertNotNull("List of attacks should not be null", attacks);
        Assert.assertFalse("List of attacks should not be empty", attacks.isEmpty());
        Multiattack multi = m.getMultiattack();
        Assert.assertNotNull("Multi-attack should not be null", multi);
        Assert.assertEquals("Multi-attack should have two combinations", 2, multi.combinations.size());

        MonsterAttack a = m.actions.get("intoxicating-touch");
        Assert.assertNotNull(a);
        MonsterDamage d = a.getDamage();
        Assert.assertEquals("cursed", d.type);
        Assert.assertEquals(1, d.getDisadvantage().size());
        Assert.assertEquals(Ability.WIS, d.getDisadvantage().get(0));
        Assert.assertEquals("target is cursed for one hour", 60, d.duration);
    }

    @Test
    public void testWraithAttacks() throws Exception {
        Map<String, Monster> compendium;

        try (InputStream jsonInput = CompendiumReader.class.getResourceAsStream("/wraith.json")) {
            compendium = CompendiumReader.mapper.readValue(jsonInput, CompendiumReader.typeRef);
            Assert.assertEquals(1, compendium.size());
        }

        // snag our single parsed value
        Monster m = compendium.values().iterator().next();
        Assert.assertNotNull(m);
        Assert.assertEquals(Type.UNDEAD, m.getType());
        Assert.assertEquals(Size.MEDIUM, m.getSize());

        List<Attack> attacks = m.getAttacks();
        Assert.assertNotNull(attacks);
        Assert.assertEquals(1, attacks.size());

        MonsterAttack a = m.actions.get("life-drain");
        Assert.assertNotNull("life-drain should be a known attack", a);

        MonsterDamage d = a.getDamage();
        Assert.assertEquals("necrotic", d.type);
        Assert.assertNotNull(a.getAdditionalEffect());
    }

    @Test
    public void testConditionAttacks() throws Exception {
        Map<String, Monster> compendium;

        try (InputStream jsonInput = CompendiumReader.class.getResourceAsStream("/gibbering-mouther.json")) {
            compendium = CompendiumReader.mapper.readValue(jsonInput, CompendiumReader.typeRef);
            Assert.assertEquals(1, compendium.size());
        }

        // snag our single parsed value
        Monster m = compendium.values().iterator().next();
        Assert.assertNotNull(m);
        Assert.assertEquals(Type.ABERRATION, m.getType());
        Assert.assertEquals(Size.MEDIUM, m.getSize());

        List<Attack> attacks = m.getAttacks();
        Assert.assertNotNull("List of attacks should not be null", attacks);
        Assert.assertFalse("List of attacks should not be empty", attacks.isEmpty());
        Multiattack multi = m.getMultiattack();
        Assert.assertNotNull("Multi-attack should not be null", multi);
        Assert.assertEquals("Multi-attack should have one combination", 1, multi.combinations.size());

        MonsterAttack a = m.actions.get("blinding-spittle");
        Assert.assertNotNull("blinding-spittle should be a known action", a);
        System.out.println(a);

        MonsterDamage d = a.getDamage();
        System.out.println(d);

        Assert.assertEquals("blinded", d.type);
        Assert.assertEquals("target is blinded for one turn", 1, d.duration);
    }
}
