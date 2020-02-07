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

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import dev.ebullient.dnd.beastiary.Beast;
import dev.ebullient.dnd.combat.Attack;
import dev.ebullient.dnd.combat.Combatant;
import dev.ebullient.dnd.mechanics.Ability;
import dev.ebullient.dnd.mechanics.Dice;
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
    public void testMonster() throws Exception {
        ClassPathResource resource = new ClassPathResource("goodMonster.json");
        Map<String, Monster> compendium;

        try (InputStream fileStream = new FileInputStream(resource.getFile())) {
            compendium = CompendiumReader.mapper.readValue(fileStream, CompendiumReader.typeRef);
            Assert.assertEquals(1, compendium.size());
        }

        // snag our single parsed value
        Monster m = compendium.values().iterator().next();
        Assert.assertNotNull(m);
        Assert.assertEquals(Type.MONSTROSITY, m.getType());
        Assert.assertEquals(Size.LARGE, m.getSize());

        // preset to predictable value ahead of participant creation
        m.hitPoints = "40";

        // See the Monster as a Beast
        Beast b = m.asBeast();

        // Make sure ability scores / modifiers were parsed properly through participant view
        Combatant c = b.createCombatant(Dice.Method.USE_AVERAGE);
        Monster.CombatantView mc = (Monster.CombatantView) c;

        Assert.assertEquals(3, c.getArmorClass());
        Assert.assertEquals(13, c.getPassivePerception());

        Assert.assertEquals(5, c.getAbilityModifier(Ability.STR));
        Assert.assertEquals(1, c.getAbilityModifier(Ability.DEX));
        Assert.assertEquals(3, c.getAbilityModifier(Ability.CON));
        Assert.assertEquals(-4, c.getAbilityModifier(Ability.INT));
        Assert.assertEquals(1, c.getAbilityModifier(Ability.WIS));
        Assert.assertEquals(-2, c.getAbilityModifier(Ability.CHA));

        // Poke on some participant behavior
        Assert.assertEquals(100, c.getRelativeHealth());

        // 1/2 health
        int startingHP = mc.hitPoints;
        Assert.assertEquals("Preset value for max health", 40, startingHP);

        int halfDamage = startingHP / 2;
        c.takeDamage(halfDamage);
        Assert.assertEquals("starting=" + startingHP + ", half damage=" + halfDamage + ", expect relative health of 50",
                50, c.getRelativeHealth());

        // 1/4 health
        startingHP = mc.hitPoints;
        halfDamage = startingHP / 2;
        c.takeDamage(halfDamage);
        Assert.assertEquals("starting=" + startingHP + ", half damage=" + halfDamage + ", expect relative health of 25",
                25, c.getRelativeHealth());

        Assert.assertNotNull(m.getMultiattack());

        List<Attack> attacks = c.getAttacks();
        Assert.assertNotNull(attacks);
        Assert.assertEquals(2, attacks.size());

        Assert.assertEquals("Known modifier should be used when no saving throw is specified",
                5, c.getSavingThrow(Ability.STR));
        Assert.assertEquals("Specified saving throw should be used",
                2, c.getSavingThrow(Ability.INT));
    }

    @Test
    public void testMeleeMonster() throws Exception {
        ClassPathResource resource = new ClassPathResource("meleeMonster.json");
        Map<String, Monster> compendium;

        try (InputStream fileStream = new FileInputStream(resource.getFile())) {
            compendium = CompendiumReader.mapper.readValue(fileStream, CompendiumReader.typeRef);
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

        Combatant c = m.asBeast().createCombatant(Dice.Method.USE_AVERAGE);
        List<Attack> attacks = c.getAttacks();
        Assert.assertNotNull(attacks);
        Assert.assertEquals(3, attacks.size());
        Assert.assertFalse("List of attacks should not contain 'melee'", attacks.toString().contains("melee"));

    }
}
