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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import dev.ebullient.dnd.bestiary.Bestiary;
import dev.ebullient.dnd.combat.Encounter;
import dev.ebullient.dnd.combat.RoundResult;
import dev.ebullient.dnd.combat.TargetSelector;
import dev.ebullient.dnd.mechanics.Dice;

@Path("/combat")
@Produces(MediaType.APPLICATION_JSON)
public class EncounterResource {

    final Bestiary beastiary;
    final CombatMetrics metrics;

    EncounterResource(BestiaryConfig config, CombatMetrics metrics) {
        this.beastiary = config.getBestiary();
        this.metrics = metrics;
    }

    @GET
    @Path("/any")
    public List<RoundResult> any() {
        return go(Dice.range(5) + 2);
    }

    @GET
    @Path("/faceoff")
    public List<RoundResult> faceoff() {
        return go(2);
    }

    @GET
    @Path("/melee")
    public List<RoundResult> melee() {
        return go(Dice.range(4) + 3);
    }

    List<RoundResult> go(int howMany) {

        Encounter encounter = beastiary.buildEncounter()
                .setHowMany(howMany)
                .setTargetSelector(pickOne(howMany))
                .build();

        List<RoundResult> results = new ArrayList<>();

        while (!encounter.isFinal()) {

            RoundResult result = encounter.oneRound();
            metrics.endRound(result);

            results.add(result);
        }
        metrics.endEncounter(encounter, results.size());

        return results;
    }

    TargetSelector pickOne(int howMany) {
        int which = Dice.range(5);
        switch (which) {
            case 4:
                return TargetSelector.SelectBiggest;
            case 3:
                return TargetSelector.SelectSmallest;
            case 2:
                return TargetSelector.SelectByHighestRelativeHealth;
            case 1:
                return TargetSelector.SelectByLowestRelativeHealth;
            default:
            case 0:
                return TargetSelector.SelectAtRandom;
        }
    }
}
