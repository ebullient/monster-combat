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
import dev.ebullient.dnd.mechanics.Ability;
import dev.ebullient.dnd.mechanics.ChallengeRating;
import dev.ebullient.dnd.mechanics.Dice;
import dev.ebullient.dnd.mechanics.Size;
import dev.ebullient.dnd.mechanics.Type;

/**
 * Monsters read from the compendium
 * Defines two
 *
 * @see CompendiumReader
 */
public class Monster implements Beast {
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
    int spellSaveDC;

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

    @JsonIgnore
    int cr;

    public String toString() {
        return name
                + "(" + size + " " + type + ")"
                + "{AC:" + armorClass
                + ",HP:" + hitPoints
                + ",STR:" + strength
                + ",DEX:" + dexterity
                + ",CON:" + constitution
                + ",INT:" + intelligence
                + ",WIS:" + wisdom
                + ",CHA:" + charisma
                + (savingThrows == null ? "" : ",SAVE:[" + savingThrows + "]")
                + ",CR:" + challengeRating
                + ",PP:" + passivePerception
                + "}";
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public int getArmorClass() {
        return armorClass;
    }

    public void setArmorClass(int armorClass) {
        this.armorClass = armorClass;
    }

    @Override
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
            this.strength = s;
            abilities.set(Ability.STR, Integer.parseInt(m.group(1)));
            modifiers.set(Ability.STR, Integer.parseInt(m.group(2)));
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
            this.dexterity = s;
            abilities.set(Ability.DEX, Integer.parseInt(m.group(1)));
            modifiers.set(Ability.DEX, Integer.parseInt(m.group(2)));
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
            this.intelligence = s;
            abilities.set(Ability.INT, Integer.parseInt(m.group(1)));
            modifiers.set(Ability.INT, Integer.parseInt(m.group(2)));
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
            this.constitution = s;
            abilities.set(Ability.CON, Integer.parseInt(m.group(1)));
            modifiers.set(Ability.CON, Integer.parseInt(m.group(2)));
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
            this.wisdom = s;
            abilities.set(Ability.WIS, Integer.parseInt(m.group(1)));
            modifiers.set(Ability.WIS, Integer.parseInt(m.group(2)));
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
            this.charisma = s;
            abilities.set(Ability.CHA, Integer.parseInt(m.group(1)));
            modifiers.set(Ability.CHA, Integer.parseInt(m.group(2)));
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
                saveThrows.set(m.group(1), Integer.parseInt(m.group(2)));
            }
        }
        this.savingThrows = savingThrows;
    }

    @Override
    public String getChallengeRating() {
        return challengeRating;
    }

    public void setChallengeRating(String challengeRating) {
        this.challengeRating = challengeRating;
        this.cr = ChallengeRating.stringToCr(challengeRating);
    }

    @Override
    public int getPassivePerception() {
        return passivePerception;
    }

    public void setPassivePerception(int passivePerception) {
        this.passivePerception = passivePerception;
    }

    public int getSpellSaveDC() {
        return spellSaveDC;
    }

    public void setSpellSaveDC(int spellSaveDC) {
        this.spellSaveDC = spellSaveDC;
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

    @Override
    public int getCR() {
        return cr;
    }

    @Override
    public int getAbilityModifier(Ability s) {
        return modifiers.get(s);
    }

    @Override
    public int getSavingThrow(Ability s) {
        int save = saveThrows.get(s);
        return save == 0 ? modifiers.get(s) : save;
    }

    @Override
    @JsonIgnore
    public List<Attack> getAttacks() {
        List<Attack> list = new ArrayList<>();
        // Use multi-attack most of the time (2 out of 3)
        if (multiattack != null && Dice.range(3) > 0) {
            String sequence = multiattack.randomCombination();
            Matcher m = ATTACK_SEQ.matcher(sequence);
            while (m.find()) {
                for (int i = 0; i < Integer.parseInt(m.group(1)); i++) {
                    if ("melee".equals(m.group(2))) {
                        list.add(actions.get(getRandomAttack()));
                    } else {
                        list.add(actions.get(m.group(2)));
                    }
                }
            }
        } else {
            list.add(actions.get(getRandomAttack()));
        }
        return list;
    }

    String getRandomAttack() {
        if (actions == null) {
            throw new IllegalArgumentException("Monster has no actions: " + this);
        }
        if (actions.size() == 1) {
            return keySet.get(0);
        }

        int random = Dice.range(actions.size());
        return keySet.get(random);
    }

}
