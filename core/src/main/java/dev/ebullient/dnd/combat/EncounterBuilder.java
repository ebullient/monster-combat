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

import dev.ebullient.dnd.beastiary.Beast;
import dev.ebullient.dnd.beastiary.Beastiary;
import dev.ebullient.dnd.mechanics.Dice;

public class EncounterBuilder {

    final Beastiary beastiary;

    int howMany = 2;
    TargetSelector selector = TargetSelector.SelectAtRandom;
    Dice.Method method = Dice.Method.ROLL;
    String challengeRating = null;

    public EncounterBuilder(Beastiary beastiary) {
        this.beastiary = beastiary;
    }

    public EncounterBuilder setMethod(Dice.Method method) {
        this.method = method;
        return this;
    }

    public EncounterBuilder setHowMany(int howMany) {
        this.howMany = howMany;
        return this;
    }

    public EncounterBuilder setChallengeRating(String challengeRating) {
        this.challengeRating = challengeRating;
        return this;
    }

    public EncounterBuilder setTargetSelector(TargetSelector selector) {
        this.selector = selector;
        return this;
    }

    public Encounter build() {
        List<EncounterCombatant> list = new ArrayList<>();
        for (int i = 0; i < howMany; i++) {
            Beast beast;
            if (challengeRating == null) {
                beast = beastiary.findOne();
            } else {
                beast = beastiary.findOneByChallengeRating(challengeRating);
            }
            list.add(new EncounterCombatant(beast, method));
        }

        return new Encounter(list, (EncounterTargetSelector) selector, method);
    }

    public static Encounter build(List<Beast> beasts, TargetSelector selector, Dice.Method method) {
        List<EncounterCombatant> list = new ArrayList<>();
        for (Beast b : beasts) {
            Encounter.validate(b);
            list.add(new EncounterCombatant(b, method));
        }
        return new Encounter(list, (EncounterTargetSelector) selector, method);
    }
}
