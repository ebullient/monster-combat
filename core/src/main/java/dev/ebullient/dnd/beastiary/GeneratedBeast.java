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
import dev.ebullient.dnd.combat.Combatant;
import dev.ebullient.dnd.mechanics.Ability;
import dev.ebullient.dnd.mechanics.ChallengeRating;
import dev.ebullient.dnd.mechanics.Dice;
import dev.ebullient.dnd.mechanics.HitPoints;
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
    final int startingHP;

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
        abilities.strength = 8 + Dice.range(cr);
        abilities.dexterity = 8 + Dice.range(cr);
        abilities.constitution = 8 + Dice.range(cr);
        abilities.intelligence = 8 + Dice.range(cr);
        abilities.wisdom = 8 + Dice.range(cr);
        abilities.charisma = 8 + Dice.range(cr);

        // Determine modifiers
        modifiers.strength = (abilities.strength - 10) / 2;
        modifiers.dexterity = (abilities.dexterity - 10) / 2;
        modifiers.constitution = (abilities.constitution - 10) / 2;
        modifiers.intelligence = (abilities.intelligence - 10) / 2;
        modifiers.wisdom = (abilities.wisdom - 10) / 2;
        modifiers.charisma = (abilities.charisma - 10) / 2;

        // Calculate based on previous values
        armorClass = calculateAC(modifiers.dexterity, cr);
        passivePerception = 10 + modifiers.intelligence;
        startingHP = HitPoints.startingHitPoints(modifiers.constitution, cr, size);
    }

    public String toString() {
        return name
                + "[" + size + " " + type
                + ", ac=" + armorClass
                + ", str=" + prettyString(abilities.strength, modifiers.strength)
                + ", dex=" + prettyString(abilities.dexterity, modifiers.dexterity)
                + ", con=" + prettyString(abilities.constitution, modifiers.constitution)
                + ", int=" + prettyString(abilities.intelligence, modifiers.intelligence)
                + ", wis=" + prettyString(abilities.wisdom, modifiers.wisdom)
                + ", cha=" + prettyString(abilities.charisma, modifiers.charisma)
                + ", cr=" + challengeRating
                + "]";
    }

    @Override
    public String getChallengeRating() {
        return challengeRating;
    }

    @Override
    public Combatant createCombatant(Dice.Method method) {
        return new GeneratedBeast.ParticipantView(this);
    }

    static class ParticipantView implements Combatant {
        final GeneratedBeast g;
        final double maxHitPoints;
        final int initiative;
        final int cr;

        int hitPoints;

        public ParticipantView(GeneratedBeast g) {
            this.g = g;
            this.hitPoints = g.startingHP;
            this.maxHitPoints = (double) hitPoints;
            this.initiative = Dice.d20() + g.modifiers.dexterity;
            this.cr = ChallengeRating.stringToCr(g.challengeRating);
        }

        @Override
        public String getName() {
            return g.name;
        }

        @Override
        public String getDescription() {
            final String description = String.format("%s %s, generated neutral", g.size, g.type);
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
        public int getArmorClass() {
            return g.armorClass;
        }

        @Override
        public int getPassivePerception() {
            return g.passivePerception;
        }

        @Override
        public int getAbilityModifier(Ability s) {
            switch (s) {
                case STR:
                    return g.modifiers.strength;
                case DEX:
                    return g.modifiers.dexterity;
                case CON:
                    return g.modifiers.constitution;
                case INT:
                    return g.modifiers.intelligence;
                case WIS:
                    return g.modifiers.wisdom;
                case CHA:
                    return g.modifiers.charisma;
            }
            return 0;
        }

        @Override
        public int getSavingThrow(Ability s) {
            return 0;
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
            // TODO Auto-generated method stub
            return null;
        }
    }

    /**
     * I am making this up, but it looks right.
     */
    static int calculateAC(int dexterity, int cr) {
        return 9 + ((dexterity + cr) / 3);
    }

    static String prettyString(int ability, int modifier) {
        return ability + "(" + modifier + ")";
    }
}
