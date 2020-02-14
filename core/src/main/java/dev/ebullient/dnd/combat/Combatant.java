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
import dev.ebullient.dnd.mechanics.Size;
import dev.ebullient.dnd.mechanics.Type;

public class Combatant {

    final Dice.Method method;
    final Beast beast;
    final int initiative;

    int hitPoints;
    final double maxHitPoints;

    Condition condition;

    public Combatant(Beast b, Dice.Method method) {
        this.beast = b;
        this.method = method;
        this.initiative = Dice.d20() + b.getAbilityModifier(Ability.DEX);

        this.hitPoints = HitPoints.startingHitPoints(b.getName(), b.getHitPoints(), method);
        this.maxHitPoints = (double) hitPoints;
    }

    Combatant(Beast b, int initiative, int startingHitPoints) {
        this.beast = b;
        this.method = Dice.Method.USE_AVERAGE;
        this.initiative = initiative;

        this.hitPoints = startingHitPoints;
        this.maxHitPoints = (double) hitPoints;
    }

    public String getName() {
        return beast.getName();
    }

    public int getCR() {
        return beast.getCR();
    }

    public int getArmorClass() {
        return beast.getArmorClass();
    }

    public Size getSize() {
        return beast.getSize();
    }

    public Type getType() {
        return beast.getType();
    }

    public int getMaxHitPoints() {
        if (condition != null) {
            return (int) maxHitPoints - condition.getMaxHitPointsDecrease();
        }
        return (int) maxHitPoints;
    }

    public int getRelativeHealth() {
        double effectiveMax = maxHitPoints;
        if (condition != null) {
            effectiveMax -= condition.getMaxHitPointsDecrease();
        }
        return (int) ((hitPoints / effectiveMax) * 100);
    }

    public boolean isAlive() {
        return hitPoints > 0;
    }

    public String toString() {
        return "Combatant["
                + beast
                + "(" + hitPoints + "/" + maxHitPoints + ")"
                + "]";
    }

    int getAbilityModifier(Ability ability) {
        return beast.getAbilityModifier(ability);
    }

    int getSavingThrow(Ability ability) {
        return beast.getSavingThrow(ability);
    }

    List<Attack> getAttacks() {
        return beast.getAttacks();
    }

    int getInitiative() {
        return initiative;
    }

    void takeDamage(int damage) {
        this.hitPoints -= damage;
        if (this.hitPoints < 0) {
            this.hitPoints = 0;
        }
    }

    Condition addCondition() {
        this.condition = new Condition();
        return condition;
    }

    boolean attackLimit() {
        if (condition != null) {
            return condition.singleAttack;
        }
        return false;
    }

    Dice.Constraint withConstraint(Ability ability) {
        if (condition != null) {
            return condition.getAbilityCheckConstraint(ability);
        }
        return Dice.Constraint.NONE;
    }

    Dice.Constraint getAttackConstraint() {
        if (condition != null) {
            return condition.getAttackConstraint();
        }
        return Dice.Constraint.NONE;
    }

    Dice.Constraint getTargetConstraint() {
        if (condition != null) {
            return condition.getTargetConstraint();
        }
        return Dice.Constraint.NONE;
    }
}
