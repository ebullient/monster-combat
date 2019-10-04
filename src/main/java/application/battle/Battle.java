/*
 * Copyright © 2019 IBM Corp. All rights reserved.
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
package application.battle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;

import application.monsters.Monster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

/**
 * Battle!
 * 1) Add monsters
 * 2) Start (returns a Flux of Rounds)
 *    a) Surprise attack Round
 *    b) Roll for initiative,
 *    c) emit new rounds until only one monster is left standing
 */
public class Battle {
    static final Logger logger = LoggerFactory.getLogger(Battle.class);
    final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");

    ArrayList<Participant> participants = new ArrayList<>();

    public Battle addMonster(Monster m) {
        Participant adversary = new Participant(m);

        for (Participant p : participants ) {
            p.adversaries.add(adversary);
            adversary.adversaries.add(p);
        }

        participants.add(adversary);
        return this;
    }

    public Flux<Round> start() {
        String id = LocalDateTime.now().format(formatter) + "-" + Integer.toHexString(participants.hashCode());

        return Flux.push(emitter -> {

            boolean keepGoing = false;

            // 0: Surprise
            logger.debug("SURPRISE: {}", participants);
            Round surprise = new Round(id, 0);
            HashSet<String> surprises = new HashSet<>();
            for ( Participant p1 : participants ) {
                for ( Participant p2 : participants ) {
                    // If p1 dexterity < p2 wisdom, AND these two haven't fought before..
                    if ( p1 != p2 && p1.getDexterity() > p2.getPerception() &&
                        surprises.add(p1.getName() + p2.getName()) &&
                        surprises.add(p2.getName() + p1.getName()) ) {
                        surprise.attack(p1, p2);
                    }
                }
            }
            keepGoing = surprise.finishRound();
            emitter.next(surprise);

            // 1: sort by initiative (then by dexterity, and then by name (just in case))
            participants.sort((p1, p2) -> {
                int delta = p1.getInitiative() - p2.getInitiative();
                if ( delta == 0 )
                    delta = p1.getDexterity() - p2.getDexterity();
                if ( delta == 0 )
                    delta = p1.getName().compareTo(p2.getName());
                return delta;
            });
            logger.debug("Participants sorted by initiative: {}", participants);

            int i = 1;
            while (keepGoing) {
                Round r = new Round(id, i++);

                // Go around in order of initiative
                for ( Participant p1 : participants ) {
                    r.attack(p1, p1.chooseTarget());
                }

                // Is there more than one monster standing?
                keepGoing = r.finishRound();
                emitter.next(r);
            }

            // 2: Set up first round
            emitter.complete();
        });
    }
}
