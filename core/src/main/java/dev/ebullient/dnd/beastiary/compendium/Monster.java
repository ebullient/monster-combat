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
import dev.ebullient.dnd.combat.Attack;
import dev.ebullient.dnd.combat.Combatant;
import dev.ebullient.dnd.mechanics.Ability;
import dev.ebullient.dnd.mechanics.ChallengeRating;
import dev.ebullient.dnd.mechanics.Dice;
import dev.ebullient.dnd.mechanics.HitPoints;
import dev.ebullient.dnd.mechanics.Size;
import dev.ebullient.dnd.mechanics.Type;

/**
 * Monsters read from the compendium
 * Defines two
 *
 * @see CompendiumReader
 */
public class Monster {
    static final Pattern ATTACK_SEQ = Pattern.compile("\\b(\\d+)\\*([-a-z]+)\\b");

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
    Map<String, MonsterAttack> actions;
    Map<String, String> description;
    int passivePerception;

    int armorClass;
    Size size;
    Type type;

    @JsonIgnore
    final Ability.All abilities = new Ability.All();

    @JsonIgnore
    final Ability.All modifiers = new Ability.All();

    @JsonIgnore
    final Ability.All saveThrows = new Ability.All();

    @JsonIgnore
    List<String> keySet = null;

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

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
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
        Matcher m = Dice.AVG_ROLL_MOD.matcher(s);
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
        Matcher m = Dice.AVG_ROLL_MOD.matcher(s);
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
        Matcher m = Dice.AVG_ROLL_MOD.matcher(s);
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
        Matcher m = Dice.AVG_ROLL_MOD.matcher(s);
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
        Matcher m = Dice.AVG_ROLL_MOD.matcher(s);
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
        Matcher m = Dice.AVG_ROLL_MOD.matcher(s);
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
            Matcher m = Attack.SAVE.matcher(savingThrows);
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

    public Map<String, MonsterAttack> getActions() {
        return actions;
    }

    public void setActions(Map<String, MonsterAttack> actions) {
        this.actions = actions;
        this.keySet = new ArrayList<>(actions.keySet());
    }

    public Multiattack getMultiattack() {
        return multiattack;
    }

    public void setMultiattack(Multiattack multiattack) {
        this.multiattack = multiattack;
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
            public Combatant createCombatant(Dice.Method method) {
                return new Monster.CombatantView(m, method);

            }
        }; // new Beast
    }

    static class CombatantView implements Combatant {

        final Monster my;
        final double maxHitPoints;
        final Dice.Method method;
        final int initiative;
        final int cr;

        int hitPoints;

        CombatantView(Monster m, Dice.Method method) {
            this.my = m;
            this.method = method;
            this.initiative = Dice.d20() + my.modifiers.dexterity;
            this.hitPoints = HitPoints.startingHitPoints(my.name, my.hitPoints, method);
            this.maxHitPoints = (double) hitPoints;
            this.cr = ChallengeRating.stringToCr(my.challengeRating);
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
        public int getCR() {
            return cr;
        }

        @Override
        public int getInitiative() {
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
        public int getMaxHitPoints() {
            return (int) maxHitPoints;
        }

        @Override
        public List<Attack> getAttacks() {
            List<Attack> list = new ArrayList<>();
            if (my.multiattack != null) {
                String sequence = my.multiattack.randomCombination();
                Matcher m = ATTACK_SEQ.matcher(sequence);
                while (m.find()) {
                    for (int i = 0; i < Integer.parseInt(m.group(1)); i++) {
                        if ("melee".equals(m.group(2))) {
                            list.add(my.actions.get(getRandomAttack()));
                        } else {
                            list.add(my.actions.get(m.group(2)));
                        }
                    }
                }
            } else {
                list.add(my.actions.get(getRandomAttack()));
            }
            return list;
        }

        @Override
        public int getSavingThrow(Ability s) {
            int save;
            switch (s) {
                case STR:
                    save = my.saveThrows.strength;
                    return save == 0 ? my.modifiers.strength : save;
                case DEX:
                    save = my.saveThrows.dexterity;
                    return save == 0 ? my.modifiers.dexterity : save;
                case CON:
                    save = my.saveThrows.constitution;
                    return save == 0 ? my.modifiers.constitution : save;
                case INT:
                    save = my.saveThrows.intelligence;
                    return save == 0 ? my.modifiers.intelligence : save;
                case WIS:
                    save = my.saveThrows.wisdom;
                    return save == 0 ? my.modifiers.wisdom : save;
                case CHA:
                    save = my.saveThrows.charisma;
                    return save == 0 ? my.modifiers.charisma : save;
            }
            return 0;
        }

        @Override
        public int getAbilityModifier(Ability s) {
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

        String getRandomAttack() {
            int random = Dice.range(my.actions.size());
            return my.keySet.get(random);
        }
    }
}
