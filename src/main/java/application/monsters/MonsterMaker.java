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
package application.monsters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import application.mechanics.Dice;

public class MonsterMaker {

    Monster m;

    public boolean isEmpty() {
        return m == null;
    }

    protected void ensureMonster() {
        if ( m == null ) {
            m = new Monster();
        }
    }

    protected String abilityValue() {
        return Integer.toString(Dice.customDie(25) + 4);
    }

    public MonsterMaker enableMultiattack() {
        ensureMonster();
        m.multiattack = true;
        return this;
    }
    public MonsterMaker setArmorClass(String text) {
        ensureMonster();
        int pos = text.indexOf('(');
        if ( pos > 0 ) {
            m.armorClass = Integer.parseInt(text.substring(0, pos).trim());
        } else {
            m.armorClass = Integer.parseInt(text.trim());
        }
        return this;
    }
    public MonsterMaker addAttack(String text) {
        ensureMonster();
        String parts[] = text.split("\\|");
        // only keep attacks with appliable damage
        if ( parts.length < 3 || parts[2].trim().isEmpty()) {
            return this;
        }

        Attack a = new Attack();
        a.name = parts[0];
        if ( parts[1].trim().isEmpty() ) {
            a.abilityModifier = 0;
        } else {
            a.abilityModifier = Integer.parseInt(parts[1]);
        }
        a.damage = parts[2];
        m.attacks.add(a);
        return this;
    }
    public MonsterMaker setCharisma(String text) {
        ensureMonster();
        m.abilities.charisma = Integer.parseInt(text);
        m.modifiers.charisma = (m.abilities.charisma - 10) / 2;
        return this;
    }
    public MonsterMaker setConstitution(String text) {
        ensureMonster();
        m.abilities.constitution = Integer.parseInt(text);
        m.modifiers.constitution = (m.abilities.constitution - 10) / 2;
        return this;
    }
    public MonsterMaker setDexterity(String text) {
        ensureMonster();
        m.abilities.dexterity = Integer.parseInt(text);
        m.modifiers.dexterity = (m.abilities.dexterity - 10) / 2;
        return this;
    }
    public MonsterMaker setHitPoints(String text) {
        // "27" or "285 (30d8 + 150)"
        final Pattern HP = Pattern.compile("(\\d+)\\s*(\\((.*)\\))?");
        ensureMonster();
        Matcher hp = HP.matcher(text);
        if ( hp.matches() ) {
            m.averageHitPoints = Integer.parseInt(hp.group(1));
            if ( hp.group(3) != null ) {
                m.dynamicHitPoints = hp.group(3);
            }
        }

        return this;
    }
    public MonsterMaker setIntelligence(String text) {
        ensureMonster();
        m.abilities.intelligence = Integer.parseInt(text);
        m.modifiers.intelligence = (m.abilities.intelligence - 10) / 2;
        return this;
    }
    public MonsterMaker setName(String text) {
        ensureMonster();
        if ( m.name == null ) {
            m.name = text;
        }
        return this;
    }
    public MonsterMaker setPassivePerception(String text) {
        ensureMonster();
        if ( text.trim().isEmpty() ) {
            m.passivePerception = 0;
        } else {
            m.passivePerception = Integer.parseInt(text);
        }
        return this;
    }
    public MonsterMaker setSize(String text) {
        ensureMonster();
        char c = text.toLowerCase().charAt(0);
        for(int i = 0; i < Monster.ALL_SIZES.length; i++) {
            if ( Monster.ALL_SIZES[i].charAt(0) == c ) {
                m.size = i;
                break;
            }
        }
        m.fullType = Monster.ALL_SIZES[m.size] + " " + m.type;
        return this;
    }
    public MonsterMaker setStrength(String text) {
        ensureMonster();
        m.abilities.strength = Integer.parseInt(text);
        m.modifiers.strength = (m.abilities.strength - 10) / 2;
        return this;
    }
    public MonsterMaker setType(String text) {
        ensureMonster();
        m.type = text.toLowerCase();
        m.fullType = Monster.ALL_SIZES[m.size] + " " + m.type;
        return this;
    }
    public MonsterMaker setWisdom(String text) {
        ensureMonster();
        m.abilities.wisdom = Integer.parseInt(text);
        m.modifiers.wisdom = (m.abilities.wisdom - 10) / 2;
        return this;
    }

    public Monster make() {
        Monster out = m;
        if ( m == null ) {
            out = m = new Monster();
            inventStats("Generated-" + Integer.toHexString(System.identityHashCode(m)));
        }
        m = null;
        return out;
    }

    /** @return a random monster type */
    String randomType() {
        int index = Dice.range(Monster.ALL_TYPES.length);
        return Monster.ALL_TYPES[index];
    }

    /** @return a random monster size */
    int randomSize() {
        return Dice.range(Monster.ALL_SIZES.length);
    }

    public MonsterMaker inventStats(String name) {
        ensureMonster();
        m.name = name;
        m.type = randomType();
        m.size = randomSize();
        m.averageHitPoints = Dice.range(300) + 10;
        m.dynamicHitPoints = "";
        m.armorClass = Dice.range(20) + 6;

        // Create strings to allow values to be parsed and set
        // with side-effects
        setStrength(abilityValue());
        setDexterity(abilityValue());
        setConstitution(abilityValue());
        setIntelligence(abilityValue());
        setWisdom(abilityValue());
        setCharisma(abilityValue());
        setPassivePerception(abilityValue());

        if ( Dice.range(2) == 0 ) {
            enableMultiattack();
        }

        addAttack("Bite|3|2d6+3+1d6");
        addAttack("Claw|3|3d6+5");
        addAttack("Tail|3|2d4+3");
        addAttack("Acid Spray||3d6");
        return this;
    }
}
