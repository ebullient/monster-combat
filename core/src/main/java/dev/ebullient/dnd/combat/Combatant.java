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
package dev.ebullient.dnd.combat;

import java.util.List;

import dev.ebullient.dnd.beastiary.Beast;
import dev.ebullient.dnd.mechanics.Ability;
import dev.ebullient.dnd.mechanics.Dice;
import dev.ebullient.dnd.mechanics.HitPoints;

public class Combatant {
    final Beast b;
    final Dice.Method method;
    final int initiative;

    int hitPoints;
    double maxHitPoints;

    public Combatant(Beast b, Dice.Method method) {
        this.b = b;
        this.method = method;
        this.initiative = Dice.d20() + b.getAbilityModifier(Ability.DEX);

        this.hitPoints = HitPoints.startingHitPoints(b.getName(), b.getHitPoints(), method);
        this.maxHitPoints = (double) hitPoints;
    }

    public Combatant(Beast b, int initiative, int startingHitPoints) {
        this.b = b;
        this.method = Dice.Method.USE_AVERAGE;
        this.initiative = initiative;

        this.hitPoints = startingHitPoints;
        this.maxHitPoints = (double) hitPoints;
    }

    public String getName() {
        return b.getName();
    }

    public void takeDamage(int damage) {
        this.hitPoints -= damage;
        if (this.hitPoints < 0) {
            this.hitPoints = 0;
        }
    }

    public int getMaxHitPoints() {
        return (int) maxHitPoints;
    }

    public int getRelativeHealth() {
        return (int) ((hitPoints / maxHitPoints) * 100);
    }

    public boolean isAlive() {
        return hitPoints > 0;
    }

    public int getInitiative() {
        return initiative;
    }

    public int getCR() {
        return b.getCR();
    }

    public int getArmorClass() {
        return b.getArmorClass();
    }

    public int getAbilityModifier(Ability ability) {
        return b.getAbilityModifier(ability);
    }

    public int getSavingThrow(Ability ability) {
        return b.getSavingThrow(ability);
    }

    public List<Attack> getAttacks() {
        return b.getAttacks();
    }

    public String toString() {
        return "Combatant["
                + b
                + "(" + hitPoints + "/" + maxHitPoints + ")"
                + "]";
    }
}
