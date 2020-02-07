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
package dev.ebullient.dnd;

import java.util.List;

import dev.ebullient.dnd.combat.Attack;
import dev.ebullient.dnd.combat.Combatant;
import dev.ebullient.dnd.mechanics.Ability;
import dev.ebullient.dnd.mechanics.Size;
import dev.ebullient.dnd.mechanics.Type;

public class MockCombatant implements Combatant {

    final String name;
    double maxHitPoints;
    int hitPoints;

    public int cr;
    public int passivePerception;
    public int initiative;

    public int armorClass;
    public Size size;
    public Type type;

    public List<Attack> attacks;

    public final Ability.All modifiers = new Ability.All();
    public final Ability.All saveThrows = new Ability.All();

    public MockCombatant(String name, int hitPoints) {
        this.name = name;
        this.hitPoints = hitPoints;
        this.maxHitPoints = (double) hitPoints;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return "Mock description";
    }

    @Override
    public int getCR() {
        return cr;
    }

    @Override
    public int getArmorClass() {
        return armorClass;
    }

    @Override
    public List<Attack> getAttacks() {
        return attacks;
    }

    @Override
    public int getInitiative() {
        return initiative;
    }

    @Override
    public int getPassivePerception() {
        return passivePerception;
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
    public int getSavingThrow(Ability s) {
        int save;
        switch (s) {
            case STR:
                save = saveThrows.strength;
                return save == 0 ? modifiers.strength : save;
            case DEX:
                save = saveThrows.dexterity;
                return save == 0 ? modifiers.dexterity : save;
            case CON:
                save = saveThrows.constitution;
                return save == 0 ? modifiers.constitution : save;
            case INT:
                save = saveThrows.intelligence;
                return save == 0 ? modifiers.intelligence : save;
            case WIS:
                save = saveThrows.wisdom;
                return save == 0 ? modifiers.wisdom : save;
            case CHA:
                save = saveThrows.charisma;
                return save == 0 ? modifiers.charisma : save;
        }
        return 0;
    }

    @Override
    public int getAbilityModifier(Ability s) {
        switch (s) {
            case STR:
                return modifiers.strength;
            case DEX:
                return modifiers.dexterity;
            case CON:
                return modifiers.constitution;
            case INT:
                return modifiers.intelligence;
            case WIS:
                return modifiers.wisdom;
            case CHA:
                return modifiers.charisma;
        }
        return 0;
    }

    public void resetHealth(int i) {
        hitPoints = i;
        maxHitPoints = (double) i;
    }
}
