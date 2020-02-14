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

import java.util.Comparator;

import dev.ebullient.dnd.mechanics.Ability;

public interface Comparators {

    public static final Comparator<Combatant> InitiativeOrder = new Comparator<Combatant>() {
        @Override
        public int compare(Combatant o1, Combatant o2) {
            // sort by initiative descending
            if (o2.getInitiative() == o1.getInitiative()) {
                // dex is already in the initiative score in a way, but .. use that as second factor (descending)
                if (o2.getAbilityModifier(Ability.DEX) == o1.getAbilityModifier(Ability.DEX)) {
                    // if all else is the same, sort by name ascending
                    if (o2.getName().equals(o1.getName())) {
                        // keep all!!
                        return System.identityHashCode(o2) - System.identityHashCode(o1);
                    }
                    return o1.getName().compareTo(o2.getName());
                }
                return o2.getAbilityModifier(Ability.DEX) - o1.getAbilityModifier(Ability.DEX);
            }
            return o2.getInitiative() - o1.getInitiative();
        }
    };

    public static final Comparator<Combatant> ChallengeRatingOrder = new Comparator<Combatant>() {
        @Override
        public int compare(Combatant o1, Combatant o2) {
            // sort by cr descending
            if (o2.getCR() == o1.getCR()) {
                // Then sort by max hit points descending
                if (o2.getMaxHitPoints() == o1.getMaxHitPoints()) {
                    // if all else is the same, sort by name ascending
                    if (o2.getName().equals(o1.getName())) {
                        // keep all!!
                        return System.identityHashCode(o2) - System.identityHashCode(o1);
                    }
                    return o1.getName().compareTo(o2.getName());
                }
                return o2.getMaxHitPoints() - o1.getMaxHitPoints();
            }
            return o2.getCR() - o1.getCR();
        }
    };

    public static final Comparator<Combatant> RelativeHealthOrder = new Comparator<Combatant>() {
        @Override
        public int compare(Combatant o1, Combatant o2) {
            // sort by relative health descending
            if (o2.getRelativeHealth() == o1.getRelativeHealth()) {
                // Then sort by max hit points descending
                if (o2.getMaxHitPoints() == o1.getMaxHitPoints()) {
                    // if all else is the same, sort by name ascending
                    if (o2.getName().equals(o1.getName())) {
                        // keep all!!
                        return System.identityHashCode(o2) - System.identityHashCode(o1);
                    }
                    return o1.getName().compareTo(o2.getName());
                }
                return o2.getMaxHitPoints() - o1.getMaxHitPoints();
            }
            return o2.getRelativeHealth() - o1.getRelativeHealth();
        }
    };
}
