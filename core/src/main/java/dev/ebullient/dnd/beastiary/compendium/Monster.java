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

import java.util.List;
import java.util.Map;

import dev.ebullient.dnd.beastiary.Beast;

/**
 * POJO for monsters read from compendium
 */
public class Monster {

    String name;
    String alignment;
    String hitPoints;
    String strength;
    String dexterity;
    String intelligence;
    String constitution;
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

    public String toString() {
        return name
            + "[" + size + " " + type
            + ", ac="+ armorClass
            + ", hp="+hitPoints
            + ", str=" + strength
            + ", dex=" + dexterity
            + ", con=" + constitution
            + ", int=" + intelligence
            + ", wis=" + wisdom
            + ", cha=" + charisma
            + ", save=[" + savingThrows + "]"
            + ", cr="+challengeRating
            + ", pp="+passivePerception
            + "]";
    }

    public boolean isValid() {
        return name != null
            && hitPoints != null
            && strength != null
            && dexterity != null
            && constitution != null
            && intelligence != null
            && wisdom != null
            && charisma != null
            && challengeRating != null
            && armorClass >= 0;
    }

    static float convertToFloat(String s) {
        switch(s) {
            case "1/8": return .125f;
            case "1/4": return .25f;
            case "1/2": return .5f;
            default: return Float.parseFloat(s);
        }
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

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getDexterity() {
        return dexterity;
    }

    public void setDexterity(String dexterity) {
        this.dexterity = dexterity;
    }

    public String getIntelligence() {
        return intelligence;
    }

    public void setIntelligence(String intelligence) {
        this.intelligence = intelligence;
    }

    public String getConstitution() {
        return constitution;
    }

    public void setConstitution(String constitution) {
        this.constitution = constitution;
    }

    public String getWisdom() {
        return wisdom;
    }

    public void setWisdom(String wisdom) {
        this.wisdom = wisdom;
    }

    public String getCharisma() {
        return charisma;
    }

    public void setCharisma(String charisma) {
        this.charisma = charisma;
    }

    public String getSavingThrows() {
        return savingThrows;
    }

    public void setSavingThrows(String savingThrows) {
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
}
