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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import application.mechanics.Dice;

/**
 * Representation of a monster.
 * Monsters have one of a few sizes, and are one of a few types.
 * They have abilities, and modifiers associated with those abilities.
 * They have an armor class, etc. They also have one or more attacks,
 * and can sometimes use more than one attack at the same time (multiattack).
 *
 * @see format for Bestiary from https://github.com/Cphrampus/DnDAppFiles
 */
public class Monster {
    static final Logger logger = LoggerFactory .getLogger(Monster.class);

    static final String[] ALL_SIZES = new String[] {
        "tiny", "small", "medium", "large", "huge", "gargantuan"
    };
    static final String[] ALL_TYPES = new String[] {
        "aberration", "beast", "celestial", "construct", "dragon", "elemental",
        "fey", "fiend", "giant", "humanoid", "monstrosity", "ooze", "plant", "undead"
    };

    static class Abilities {
        int strength;
        int dexterity;
        int constitution;
        int intelligence;
        int wisdom;
        int charisma;
    }

    static class Modifiers {
        int strength;
        int dexterity;
        int constitution;
        int intelligence;
        int wisdom;
        int charisma;
    }

    String name;

    int size;
    String type;

    String fullType;
    String hitPointStats;

    int armorClass;
    int averageHitPoints = 0;
    String dynamicHitPoints;
    int passivePerception;
    Abilities abilities = new Abilities();
    Modifiers modifiers = new Modifiers();
    boolean multiattack = false;
    ArrayList<Attack> attacks = new ArrayList<>();

	public boolean isValid() {
        return armorClass > 0
         && averageHitPoints > 0
         && passivePerception > 0
         && dynamicHitPoints != null
         && attacks.size() > 0;
	}

    public String getName() {
        return name;
    }
    public String getFullType() {
        return fullType;
    }
    public String getHitPointStats() {
        String desc = hitPointStats;
        if ( desc == null ) {
            StringBuilder str = new StringBuilder();
            str.append(averageHitPoints);
            if ( dynamicHitPoints != null && ! dynamicHitPoints.isEmpty() ) {
                str.append('(').append(dynamicHitPoints).append(')');
            }
            desc = hitPointStats = str.toString();
        }
        return desc;
    }

    public int getArmorClass() {
        return armorClass;
    }

    public int getDexterity() {
        return abilities.dexterity;
    }
    public int getDexterityModifier() {
        return modifiers.dexterity;
    }

    public int getHitPoints() {
        if ( ! dynamicHitPoints.isEmpty() ) {
            return Dice.roll(dynamicHitPoints);
        }
        return averageHitPoints;
    }

    public int getPassivePerception() {
        return passivePerception;
    }

    public List<Attack> attack() {
        ArrayList<Attack> list = new ArrayList<>();
        int max = attacks.size();

        if ( multiattack && max > 1 ) {
            list.add(attacks.get(0));
            list.add(attacks.get(Dice.range(max)));
        } else {
            list.add(attacks.get(Dice.range(max)));
        }
        return list;
    }

    public String dumpStats() {
        return String.format("%s\nSTR %d (%d), DEX %d (%d), CON %d (%d), INT %d (%d), WIS %d (%d), CHA %d (%d)\nMultiattack: %b, Attacks: %s",
            this.toString(),
            abilities.strength, modifiers.strength,
            abilities.dexterity, modifiers.dexterity,
            abilities.constitution, modifiers.constitution,
            abilities.intelligence, modifiers.intelligence,
            abilities.wisdom, modifiers.wisdom,
            abilities.charisma, modifiers.charisma,
            multiattack, attacks);
    }

    public String toString() {
        return String.format("%s (%s), AC: %d, HP: %s", name, getFullType(), armorClass, getHitPointStats());
    }
}
