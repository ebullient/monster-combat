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

import java.util.ArrayList;
import java.util.List;

import dev.ebullient.dnd.mechanics.Dice;

/**
 * For combat rounds, we're dealing with very small sets (< 10)
 */
interface EncounterTargetSelector extends TargetSelector {

    EncounterCombatant chooseTarget(EncounterCombatant p, List<EncounterCombatant> list);

    static String targetSelectorToString(TargetSelector t, int numCombatants) {
        if (numCombatants == 2) {
            return "FaceOff";
        }
        return t.toString();
    }

    static final EncounterTargetSelector SelectByHighestRelativeHealth = new EncounterTargetSelector() {

        public EncounterCombatant chooseTarget(EncounterCombatant p, List<EncounterCombatant> list) {
            List<EncounterCombatant> targets = new ArrayList<>(list);
            targets.remove(p);

            if (targets.isEmpty()) {
                return null;
            }

            targets.sort(EncounterComparators.RelativeHealthOrder);
            return targets.get(0);
        }

        public String toString() {
            return "HighestHealth";
        }
    };

    static final EncounterTargetSelector SelectByLowestRelativeHealth = new EncounterTargetSelector() {

        public EncounterCombatant chooseTarget(EncounterCombatant p, List<EncounterCombatant> list) {
            List<EncounterCombatant> targets = new ArrayList<>(list);
            targets.remove(p);
            if (targets.isEmpty()) {
                return null;
            } else if (targets.size() == 1) {
                return targets.get(0);
            }

            targets.sort(EncounterComparators.RelativeHealthOrder);
            return targets.get(targets.size() - 1);
        }

        public String toString() {
            return "LowestHealth";
        }
    };

    static final EncounterTargetSelector SelectByHighestChallengeRating = new EncounterTargetSelector() {

        public EncounterCombatant chooseTarget(EncounterCombatant p, List<EncounterCombatant> list) {
            List<EncounterCombatant> targets = new ArrayList<>(list);
            targets.remove(p);
            if (targets.isEmpty()) {
                return null;
            }

            targets.sort(EncounterComparators.ChallengeRatingOrder);
            return targets.get(0);
        }

        public String toString() {
            return "HighestCR";
        }
    };

    static final EncounterTargetSelector SelectByLowestChallengeRating = new EncounterTargetSelector() {

        public EncounterCombatant chooseTarget(EncounterCombatant p, List<EncounterCombatant> list) {
            List<EncounterCombatant> targets = new ArrayList<>(list);
            targets.remove(p);
            if (targets.isEmpty()) {
                return null;
            } else if (targets.size() == 1) {
                return targets.get(0);
            }

            targets.sort(EncounterComparators.ChallengeRatingOrder);
            return targets.get(targets.size() - 1);
        }

        public String toString() {
            return "LowestCR";
        }
    };

    static final EncounterTargetSelector SelectBiggest = new EncounterTargetSelector() {

        public EncounterCombatant chooseTarget(EncounterCombatant p, List<EncounterCombatant> list) {
            List<EncounterCombatant> targets = new ArrayList<>(list);
            targets.remove(p);
            if (targets.isEmpty()) {
                return null;
            }

            targets.sort(EncounterComparators.SizeOrder);
            return targets.get(0);
        }

        public String toString() {
            return "BiggestFirst";
        }
    };

    static final EncounterTargetSelector SelectSmallest = new EncounterTargetSelector() {

        public EncounterCombatant chooseTarget(EncounterCombatant p, List<EncounterCombatant> list) {
            List<EncounterCombatant> targets = new ArrayList<>(list);
            targets.remove(p);
            if (targets.isEmpty()) {
                return null;
            } else if (targets.size() == 1) {
                return targets.get(0);
            }

            targets.sort(EncounterComparators.SizeOrder);
            return targets.get(targets.size() - 1);
        }

        public String toString() {
            return "SmallestFirst";
        }
    };

    static final EncounterTargetSelector SelectAtRandom = new EncounterTargetSelector() {

        public EncounterCombatant chooseTarget(EncounterCombatant p, List<EncounterCombatant> list) {
            List<EncounterCombatant> targets = new ArrayList<>(list);
            targets.remove(p);
            if (targets.isEmpty()) {
                return null;
            } else if (targets.size() == 1) {
                return targets.get(0);
            }

            return targets.get(Dice.range(targets.size()));
        }

        public String toString() {
            return "Random";
        }
    };
}
