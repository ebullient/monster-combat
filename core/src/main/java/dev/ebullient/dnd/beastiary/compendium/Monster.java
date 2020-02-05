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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;

import dev.ebullient.dnd.beastiary.Beast;
import dev.ebullient.dnd.mechanics.Dice;

/**
 * POJO for monsters read from compendium
 */
public class Monster {
    final static Pattern AVG_ROLL_MOD = Pattern.compile("(\\d+)(?:\\(([-+d0-9]+)\\))?");
    final static Pattern SAVE = Pattern.compile("([A-Z]+)\\(([-+0-9]+)\\)");
    final static Pattern ATTACK_SEQ = Pattern.compile("\\b(\\d+)\\*([-a-z]+)\\b");

    static class Stats {
        int strength;
        int dexterity;
        int constitution;
        int intelligence;
        int wisdom;
        int charisma;
    }

    String name;
    String alignment;
    String hitPoints;
    String strength;
    String dexterity;
    String constitution;
    String intelligence;
    String wisdom;
    String charisma;
    String savingThrows;
    String challengeRating;
    Multiattack multiattack;
    Map<String, Attack> actions;
    Map<String, String> description;
    int passivePerception;

    int armorClass;
    Beast.Size size;
    Beast.Type type;

    @JsonIgnore
    Stats abilities = new Stats();

    @JsonIgnore
    Stats modifiers = new Stats();

    @JsonIgnore
    Stats saveThrows = new Stats();

    public String toString() {
        return name
                + "[" + size + " " + type
                + ", ac=" + armorClass
                + ", hp=" + hitPoints
                + ", str=" + strength
                + ", dex=" + dexterity
                + ", con=" + constitution
                + ", int=" + intelligence
                + ", wis=" + wisdom
                + ", cha=" + charisma
                + ", save=[" + savingThrows + "]"
                + ", cr=" + challengeRating
                + ", pp=" + passivePerception
                + "]";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment.trim();
    }

    public Beast.Size getSize() {
        return size;
    }

    public void setSize(Beast.Size size) {
        this.size = size;
    }

    public Beast.Type getType() {
        return type;
    }

    public void setType(Beast.Type type) {
        this.type = type;
    }

    public int getArmorClass() {
        return armorClass;
    }

    public void setArmorClass(int armorClass) {
        this.armorClass = armorClass;
    }

    public String getHitPoints() {
        return hitPoints;
    }

    public void setHitPoints(String hitPoints) {
        this.hitPoints = hitPoints;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String s) {
        Matcher m = AVG_ROLL_MOD.matcher(s);
        if (m.matches()) {
            abilities.strength = Integer.parseInt(m.group(1));
            modifiers.strength = Integer.parseInt(m.group(2));
            this.strength = s;
        } else {
            throw new IllegalArgumentException(name + " has unparseable statistics " + s);
        }
    }

    public String getDexterity() {
        return dexterity;
    }

    public void setDexterity(String s) {
        Matcher m = AVG_ROLL_MOD.matcher(s);
        if (m.matches()) {
            abilities.dexterity = Integer.parseInt(m.group(1));
            modifiers.dexterity = Integer.parseInt(m.group(2));
            this.dexterity = s;
        } else {
            throw new IllegalArgumentException(name + " has unparseable statistics " + s);
        }
    }

    public String getIntelligence() {
        return intelligence;
    }

    public void setIntelligence(String s) {
        Matcher m = AVG_ROLL_MOD.matcher(s);
        if (m.matches()) {
            abilities.intelligence = Integer.parseInt(m.group(1));
            modifiers.intelligence = Integer.parseInt(m.group(2));
            this.intelligence = s;
        } else {
            throw new IllegalArgumentException(name + " has unparseable statistics " + s);
        }
    }

    public String getConstitution() {
        return constitution;
    }

    public void setConstitution(String s) {
        Matcher m = AVG_ROLL_MOD.matcher(s);
        if (m.matches()) {
            abilities.constitution = Integer.parseInt(m.group(1));
            modifiers.constitution = Integer.parseInt(m.group(2));
            this.constitution = s;
        } else {
            throw new IllegalArgumentException(name + " has unparseable statistics " + s);
        }
    }

    public String getWisdom() {
        return wisdom;
    }

    public void setWisdom(String s) {
        Matcher m = AVG_ROLL_MOD.matcher(s);
        if (m.matches()) {
            abilities.wisdom = Integer.parseInt(m.group(1));
            modifiers.wisdom = Integer.parseInt(m.group(2));
            this.wisdom = s;
        } else {
            throw new IllegalArgumentException(name + " has unparseable statistics " + s);
        }
    }

    public String getCharisma() {
        return charisma;
    }

    public void setCharisma(String s) {
        Matcher m = AVG_ROLL_MOD.matcher(s);
        if (m.matches()) {
            abilities.charisma = Integer.parseInt(m.group(1));
            modifiers.charisma = Integer.parseInt(m.group(2));
            this.charisma = s;
        } else {
            throw new IllegalArgumentException(name + " has unparseable statistics " + s);
        }
    }

    public String getSavingThrows() {
        return savingThrows;
    }

    public void setSavingThrows(String savingThrows) {
        if (savingThrows != null) {
            Matcher m = SAVE.matcher(savingThrows);
            while (m.find()) {
                switch (m.group(1)) {
                    case "STR":
                        saveThrows.strength = Integer.parseInt(m.group(2));
                        break;
                    case "DEX":
                        saveThrows.dexterity = Integer.parseInt(m.group(2));
                        break;
                    case "CON":
                        saveThrows.constitution = Integer.parseInt(m.group(2));
                        break;
                    case "INT":
                        saveThrows.intelligence = Integer.parseInt(m.group(2));
                        break;
                    case "WIS":
                        saveThrows.wisdom = Integer.parseInt(m.group(2));
                        break;
                    case "CHA":
                        saveThrows.charisma = Integer.parseInt(m.group(2));
                        break;
                }
            }
        }

        this.savingThrows = savingThrows;
    }

    public String getChallengeRating() {
        return challengeRating;
    }

    public void setChallengeRating(String challengeRating) {
        this.challengeRating = challengeRating;
    }

    public int getPassivePerception() {
        return passivePerception;
    }

    public void setPassivePerception(int passivePerception) {
        this.passivePerception = passivePerception;
    }

    public Map<String, String> getDescription() {
        return description;
    }

    public void setDescription(Map<String, String> description) {
        this.description = description;
    }

    public Map<String, Attack> getActions() {
        return actions;
    }

    public void setActions(Map<String, Attack> actions) {
        this.actions = actions;
    }

    public Multiattack getMultiattack() {
        return multiattack;
    }

    public void setMultiattack(Multiattack multiattack) {
        this.multiattack = multiattack;
    }

    private int startingHitPoints() {
        if (hitPoints != null) {
            Matcher m = AVG_ROLL_MOD.matcher(hitPoints);
            if (m.matches()) {
                return m.group(2) == null ? Integer.parseInt(m.group(1)) : Dice.roll(m.group(2));
            }
        }
        throw new IllegalArgumentException("Bad hitpoints string [" + hitPoints + "] or perma-dead creature " + name);
    }

    @JsonIgnore
    public Beast asBeast() {
        final Monster m = this;

        return new Beast() {
            @Override
            public String getChallengeRating() {
                return m.getChallengeRating();
            }

            @Override
            public Beast.Participant createParticipant() {
                return new Monster.Participant(m);

            }
        }; // new Beast
    }

    static class Participant implements Beast.Participant {

        final Monster my;
        final double maxHitPoints;
        int hitPoints;

        Participant(Monster m) {
            my = m;
            hitPoints = my.startingHitPoints();
            maxHitPoints = (double) hitPoints;
        }

        @Override
        public String getName() {
            return my.name;
        }

        @Override
        public String getDescription() {
            final String description = my.name + ", " + my.description.get("General");
            return description;
        }

        @Override
        public int getInitiative() {
            final int initiative = Dice.d20() + getModifier(Beast.Statistic.DEX);
            return initiative;
        }

        @Override
        public int getPassivePerception() {
            return my.passivePerception;
        }

        @Override
        public int getArmorClass() {
            return my.armorClass;
        }

        @Override
        public void takeDamage(int damage) {
            this.hitPoints -= damage;
            if (this.hitPoints < 0) {
                this.hitPoints = 0;
            }
        }

        @Override
        public boolean isAlive() {
            return hitPoints > 0;
        }

        @Override
        public int getRelativeHealth() {
            return (int) ((hitPoints / maxHitPoints) * 100);
        }

        @Override
        public List<Beast.Attack> getAttacks() {
            List<Beast.Attack> list = new ArrayList<>();
            if (my.multiattack != null) {
                String sequence = my.multiattack.randomCombination();
                Matcher m = ATTACK_SEQ.matcher(sequence);
                while (m.find()) {
                    for (int i = 0; i < Integer.parseInt(m.group(1)); i++) {
                        list.add(my.actions.get(m.group(2)));
                    }
                }
            } else {
                list.add(my.actions.values().iterator().next());
            }
            return list;
        }

        @Override
        public int getSavingThrow(Beast.Statistic s) {
            switch (s) {
                case STR:
                    return my.saveThrows.strength;
                case DEX:
                    return my.saveThrows.dexterity;
                case CON:
                    return my.saveThrows.constitution;
                case INT:
                    return my.saveThrows.intelligence;
                case WIS:
                    return my.saveThrows.wisdom;
                case CHA:
                    return my.saveThrows.charisma;
            }
            return 0;
        }

        @Override
        public int getModifier(Beast.Statistic s) {
            switch (s) {
                case STR:
                    return my.modifiers.strength;
                case DEX:
                    return my.modifiers.dexterity;
                case CON:
                    return my.modifiers.constitution;
                case INT:
                    return my.modifiers.intelligence;
                case WIS:
                    return my.modifiers.wisdom;
                case CHA:
                    return my.modifiers.charisma;
            }
            return 0;
        }

        @Override
        public int getAbility(Beast.Statistic s) {
            switch (s) {
                case STR:
                    return my.abilities.strength;
                case DEX:
                    return my.abilities.dexterity;
                case CON:
                    return my.abilities.constitution;
                case INT:
                    return my.abilities.intelligence;
                case WIS:
                    return my.abilities.wisdom;
                case CHA:
                    return my.abilities.charisma;
            }
            return 0;
        }
    }
}
