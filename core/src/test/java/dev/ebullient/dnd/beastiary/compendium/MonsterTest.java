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
import dev.ebullient.dnd.beastiary.Beast.Statistic;

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
        Assert.assertEquals(Beast.Type.MONSTROSITY, m.getType());
        Assert.assertEquals(Beast.Size.LARGE, m.getSize());

        // See the Monster as a Beast
        Beast b = m.asBeast();

        // Make sure ability scores / modifiers were parsed properly through participant view
        Beast.Participant p = b.createParticipant();
        Monster.Participant mp = (Monster.Participant) p;

        Assert.assertEquals(3, p.getArmorClass());
        Assert.assertEquals(13, p.getPassivePerception());

        Assert.assertEquals(20, p.getAbility(Statistic.STR));
        Assert.assertEquals(12, p.getAbility(Statistic.DEX));
        Assert.assertEquals(17, p.getAbility(Statistic.CON));
        Assert.assertEquals(3, p.getAbility(Statistic.INT));
        Assert.assertEquals(12, p.getAbility(Statistic.WIS));
        Assert.assertEquals(7, p.getAbility(Statistic.CHA));

        Assert.assertEquals(5, p.getModifier(Statistic.STR));
        Assert.assertEquals(1, p.getModifier(Statistic.DEX));
        Assert.assertEquals(3, p.getModifier(Statistic.CON));
        Assert.assertEquals(-4, p.getModifier(Statistic.INT));
        Assert.assertEquals(1, p.getModifier(Statistic.WIS));
        Assert.assertEquals(-2, p.getModifier(Statistic.CHA));
        Assert.assertEquals(3, p.getArmorClass());
        Assert.assertEquals(13, p.getPassivePerception());

        // Poke on some participant behavior
        Assert.assertEquals(100, p.getRelativeHealth());

        // 1/2 health
        p.takeDamage(mp.hitPoints / 2);
        Assert.assertEquals(50, p.getRelativeHealth());

        // 1/4 health
        p.takeDamage(mp.hitPoints / 2);
        Assert.assertEquals(25, p.getRelativeHealth());

        Assert.assertNotNull(m.getMultiattack());

        List<Beast.Attack> attacks = p.getAttacks();
        Assert.assertNotNull(attacks);
        Assert.assertEquals(2, attacks.size());
    }

}
