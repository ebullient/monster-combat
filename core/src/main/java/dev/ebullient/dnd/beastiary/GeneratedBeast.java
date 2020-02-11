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
package dev.ebullient.dnd.beastiary;

import java.util.List;

import dev.ebullient.dnd.combat.Attack;
import dev.ebullient.dnd.mechanics.Ability;
import dev.ebullient.dnd.mechanics.ChallengeRating;
import dev.ebullient.dnd.mechanics.Dice;
import dev.ebullient.dnd.mechanics.Size;
import dev.ebullient.dnd.mechanics.Type;

public class GeneratedBeast implements Beast {

    final String name = "Generated-" + Integer.toHexString(System.identityHashCode(this));
    final Ability.All abilities = new Ability.All();
    final Ability.All modifiers = new Ability.All();

    final Size size;
    final Type type;
    final String challengeRating;
    final int cr;

    final int armorClass;
    final int passivePerception;

    public GeneratedBeast() {
        size = Size.getOne();
        type = Type.getOne();

        // Use size and type to calculate a challenge rating,
        cr = ChallengeRating.calculateCR(size, type);
        // then make a pretty string.
        challengeRating = ChallengeRating.crToString(cr);

        // Roll our ability scores: this will be random w/in range,
        // but higher level monsters can get higher scores
        for (Ability a : Ability.allValues) {
            int value = 8 + Dice.range(cr);
            abilities.set(a, 8 + Dice.range(cr));
            modifiers.set(a, (value - 10) / 2);
        }

        // Calculate based on previous values
        armorClass = calculateAC(modifiers.get(Ability.DEX), cr);
        passivePerception = 10 + modifiers.get(Ability.INT);
    }

    public String toString() {
        return name
                + "[" + size + " " + type
                + ", ac=" + armorClass
                + ", str=" + prettyString(Ability.STR)
                + ", dex=" + prettyString(Ability.DEX)
                + ", con=" + prettyString(Ability.CON)
                + ", int=" + prettyString(Ability.INT)
                + ", wis=" + prettyString(Ability.WIS)
                + ", cha=" + prettyString(Ability.CHA)
                + ", cr=" + challengeRating
                + "]";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getHitPoints() {
        return "0";
    }

    @Override
    public String getChallengeRating() {
        return challengeRating;
    }

    @Override
    public int getCR() {
        return cr;
    }

    @Override
    public Size getSize() {
        return size;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int getArmorClass() {
        return armorClass;
    }

    @Override
    public int getPassivePerception() {
        return passivePerception;
    }

    @Override
    public int getAbilityModifier(Ability a) {
        return modifiers.get(a);
    }

    @Override
    public int getSavingThrow(Ability a) {
        return 0;
    }

    @Override
    public List<Attack> getAttacks() {
        return null;
    }

    String prettyString(Ability a) {
        return abilities.get(a) + "(" + modifiers.get(a) + ")";
    }

    /**
     * I am making this up, but it looks right.
     */
    static int calculateAC(int dexterity, int cr) {
        return 9 + ((dexterity + cr) / 3);
    }
}
