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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.ebullient.dnd.mechanics.Dice;

public class Beastiary {

    int totalCount;
    List<Beast> allBeasts = new ArrayList<>(500);
    Map<String, List<Beast>> beastsByChallengeRating = new HashMap<>(500);

    /**
     * Add one beast to the Beastiary
     */
    public Beast save(Beast b) {
        allBeasts.add(b);

        String cr = b.getChallengeRating();
        beastsByChallengeRating
                .computeIfAbsent(cr, k -> new ArrayList<>())
                .add(b);

        totalCount++;
        return b;
    }

    /**
     * @return a random monster
     */
    public Beast findOne() {
        if (allBeasts.isEmpty()) {
            return null;
        } else if (allBeasts.size() == 1) {
            return allBeasts.get(0);
        }
        return allBeasts.get(Dice.range(allBeasts.size()));
    }

    /**
     * @return a random beast with the requested challenge rating
     */
    public Beast findOneByChallengeRating(String cr) {
        List<Beast> beasts = beastsByChallengeRating.get(cr);
        if (beasts.isEmpty()) {
            return null;
        } else if (allBeasts.size() == 1) {
            return beasts.get(0);
        }
        return beasts.get(Dice.range(beasts.size()));
    }

    public String toString() {
        return new StringBuilder()
                .append("Beastiary contains ~")
                .append(totalCount)
                .append(" beasts")
                .toString();
    }
}
