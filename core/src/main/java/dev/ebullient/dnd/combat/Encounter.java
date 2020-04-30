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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ebullient.dnd.bestiary.Beast;
import dev.ebullient.dnd.mechanics.Dice;
import dev.ebullient.dnd.mechanics.Type;

public class Encounter {
    static final Logger logger = LoggerFactory.getLogger(Encounter.class);
    final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");

    final String id = LocalDateTime.now().format(formatter) + "-" + Integer.toHexString(this.hashCode());

    final EncounterTargetSelector selector;
    final Dice.Method method;
    final List<EncounterCombatant> initiativeOrder;
    final int numCombatants;
    final int numTypes;
    final int crDelta;
    final int sizeDelta;

    Encounter(List<EncounterCombatant> combatants, EncounterTargetSelector selector, Dice.Method method) {
        this.initiativeOrder = new ArrayList<>(combatants);
        this.initiativeOrder.sort(EncounterComparators.InitiativeOrder);
        this.numCombatants = initiativeOrder.size();
        this.selector = selector;
        this.method = method;

        EncounterCombatant first = initiativeOrder.iterator().next();
        int maxCR = first.beast.getCR();
        int minCR = maxCR;
        int maxSize = first.beast.getSize().ordinal();
        int minSize = maxSize;
        Set<Type> types = new HashSet<>();

        for (EncounterCombatant x : combatants) {
            types.add(x.beast.getType());
            maxCR = Math.max(x.beast.getCR(), maxCR);
            minCR = Math.min(x.beast.getCR(), minCR);
            maxSize = Math.max(x.beast.getSize().ordinal(), maxSize);
            minSize = Math.min(x.beast.getSize().ordinal(), minSize);
        }

        this.crDelta = maxCR - minCR;
        this.sizeDelta = maxSize - minSize;
        this.numTypes = types.size();
    }

    public boolean isFinal() {
        return initiativeOrder.size() <= 1;
    }

    public int getNumCombatants() {
        return numCombatants;
    }

    public int getSizeDelta() {
        return sizeDelta;
    }

    public int getCrDelta() {
        return crDelta;
    }

    public int getNumTypes() {
        return numTypes;
    }

    public String getSelector() {
        return EncounterTargetSelector.targetSelectorToString(selector, numCombatants);
    }

    public int getNumSurvivors() {
        return initiativeOrder.size();
    }

    public RoundResult oneRound() {
        logger.debug("oneRound: {} {}", initiativeOrder, id);

        EncounterRoundResult result = new EncounterRoundResult(initiativeOrder, selector, method, id);
        result.go();

        initiativeOrder.retainAll(result.survivors);

        logger.debug("oneRound: survivors {} {}", result.survivors, id);
        return result;
    }

    /**
     * Make sure a beast satisfies combat requirements so we don't have to check
     * for bad/missing conditions during combat rounds (above).
     */
    public static void validate(Beast beast) {
        for (Attack a : beast.getAttacks()) {
            if (a == null) {
                throw new IllegalArgumentException(
                        String.format("Beast %s has a null element in list of attacks: %s",
                                beast.getName(), beast.getAttacks()));
            }

            if (a.getAttackModifier() == 0 && a.getSavingThrow() == null) {
                throw new IllegalArgumentException(
                        String.format("Beast %s attack %s does not specify an attack modifier or a saving throw: %s",
                                beast.getName(), a.getName(), a.getDescription()));
            }

            if (a.getAttackModifier() != 0 && a.getSavingThrow() != null) {
                throw new IllegalArgumentException(
                        String.format("Beast %s attack %s specifies both an attack modifier and a saving throw: %s",
                                beast.getName(), a.getName(), a.getDescription()));
            }

            String savingThrow = a.getSavingThrow();
            if (savingThrow != null) {
                Matcher m = Attack.SAVE.matcher(savingThrow);
                if (!m.matches()) {
                    throw new IllegalArgumentException(
                            String.format("Beast %s attack %s specifies an invalid saving throw (%s): %s",
                                    beast.getName(), a.getName(), savingThrow, a.getDescription()));
                }
            }

            Attack.Damage damage = a.getDamage();
            if (damage == null) {
                throw new IllegalArgumentException(
                        String.format("Beast %s attack %s does not specify damage: %s",
                                beast.getName(), a.getName(), a.getDescription()));
            }

            if (a.getAttackModifier() != 0 && damage.getType() == null) {
                throw new IllegalArgumentException(
                        String.format("Beast %s attack %s specifies an attack modifier but no hit damage: %s",
                                beast.getName(), a.getName(), a.getDescription()));
            }

            savingThrow = damage.getSavingThrow();
            if (savingThrow != null) {
                Matcher m = Attack.SAVE.matcher(savingThrow);
                if (!m.matches()) {
                    throw new IllegalArgumentException(
                            String.format("Beast %s attack %s specifies an invalid saving throw (%s): %s",
                                    beast.getName(), a.getName(), damage.getSavingThrow(), a.getDescription()));
                }
            }
        }
    }
}
