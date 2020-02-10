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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.ebullient.dnd.mechanics.Ability;
import dev.ebullient.dnd.mechanics.Dice;

public class Condition {
    Dice.Constraint onAttack;
    Dice.Constraint asTarget;
    final Set<Ability> advantage = new HashSet<>();
    final Set<Ability> disadvantage = new HashSet<>();
    int duration;
    boolean singleAttack;

    Condition disadvantage(List<Ability> abilities) {
        if (abilities != null && !abilities.isEmpty()) {
            for (Ability a : abilities) {
                this.disadvantage.add(a);
            }
        }
        return this;
    }

    Condition disadvantage(Ability... abilities) {
        for (Ability a : abilities) {
            this.disadvantage.add(a);
        }
        return this;
    }

    Condition forDuration(int duration) {
        this.duration = duration;
        return this;
    }

    Condition onAttack(Dice.Constraint constraint) {
        this.onAttack = constraint;
        return this;
    }

    Condition asTarget(Dice.Constraint constraint) {
        this.asTarget = constraint;
        return this;
    }

    public Condition singleAttack() {
        this.singleAttack = true;
        return this;
    }
}
