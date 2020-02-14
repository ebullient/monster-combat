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
public interface TargetSelector {

    public Combatant chooseTarget(Combatant p, List<Combatant> list);

    public static final TargetSelector SelectByHighestRelativeHealth = new TargetSelector() {

        public Combatant chooseTarget(Combatant p, List<Combatant> initiativeOrder) {
            List<Combatant> targets = new ArrayList<>(initiativeOrder);
            targets.remove(p);

            if (targets.isEmpty()) {
                return null;
            }

            targets.sort(Comparators.RelativeHealthOrder);
            return targets.get(0);
        }
    };

    public static final TargetSelector SelectByLowestRelativeHealth = new TargetSelector() {

        public Combatant chooseTarget(Combatant p, List<Combatant> initiativeOrder) {
            List<Combatant> targets = new ArrayList<>(initiativeOrder);
            targets.remove(p);
            if (targets.isEmpty()) {
                return null;
            } else if (targets.size() == 1) {
                return targets.get(0);
            }

            targets.sort(Comparators.RelativeHealthOrder);
            return targets.get(targets.size() - 1);
        }
    };

    public static final TargetSelector SelectByHighestChallengeRating = new TargetSelector() {

        public Combatant chooseTarget(Combatant p, List<Combatant> initiativeOrder) {
            List<Combatant> targets = new ArrayList<>(initiativeOrder);
            targets.remove(p);
            if (targets.isEmpty()) {
                return null;
            }

            targets.sort(Comparators.ChallengeRatingOrder);
            return targets.get(0);
        }
    };

    public static final TargetSelector SelectByLowestChallengeRating = new TargetSelector() {

        public Combatant chooseTarget(Combatant p, List<Combatant> initiativeOrder) {
            List<Combatant> targets = new ArrayList<>(initiativeOrder);
            targets.remove(p);
            if (targets.isEmpty()) {
                return null;
            } else if (targets.size() == 1) {
                return targets.get(0);
            }

            targets.sort(Comparators.ChallengeRatingOrder);
            return targets.get(targets.size() - 1);
        }
    };

    public static final TargetSelector SelectAtRandom = new TargetSelector() {

        public Combatant chooseTarget(Combatant p, List<Combatant> initiativeOrder) {
            List<Combatant> targets = new ArrayList<>(initiativeOrder);
            targets.remove(p);
            if (targets.isEmpty()) {
                return null;
            } else if (targets.size() == 1) {
                return targets.get(0);
            }

            return targets.get(Dice.range(targets.size()));
        }
    };
}
