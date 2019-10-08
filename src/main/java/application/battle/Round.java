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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import application.mechanics.Dice;
import application.monsters.Attack;
import io.micrometer.core.instrument.Timer.Sample;

/**
 * Record the result of attacks between monsters.
 * The Battle will iterate over participating monsters
 * in the appropriate order. This will perform the mechanics
 * of the attack, and will record the outcome.
 */
public class Round {
    final String id;
    final int number;
    final ArrayList<Result> attackResults = new ArrayList<>();
    final ArrayList<String> outcome = new ArrayList<>();
    final BattleMetrics metrics;

    Participant victor;

    @JsonIgnore
    Sample start;

    @JsonIgnore
    final HashSet<Participant> participants = new HashSet<>();

    public Round(String id, int number, BattleMetrics metrics) {
        this.id = id;
        this.number = number;
        this.metrics = metrics;
        this.start = metrics.startRound(this);
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
    /**
     * @return the id
     */
    public int getNumber() {
        return number;
    }

    public List<Result> getResults() {
        return attackResults;
    }

    public List<String> getOutcome() {
        return outcome;
    }
	public Participant getVictor() {
		return victor;
    }

    public void attack(Participant attacker, Participant target) {
        participants.add(attacker);
        participants.add(target);

        if ( attacker.hitPoints <= 0 || target.hitPoints <= 0 ) {
            return;
        }

        List<Attack> attacks = attacker.attack();
        for (Attack a : attacks) {
            attemptAttack(attacker, a, target);
        }
    }

    void attemptAttack(Participant attacker, Attack a, Participant target) {
        Result r = new Result();
        r.attackerName = attacker.getName();
        r.attackName = a.getName();
        r.targetName = target.getName();

        int attackRoll = Dice.d20();
        if ( attackRoll == 1 ) {
            r.hit = false;
            r.critical = true;
        } else if ( attackRoll == 20 ) {
            r.hit = true;
            r.critical = true;
        } else {
            attackRoll += a.getAbilityModifier();
            int targetValue = target.getArmorClass();
            r.hit = attackRoll >= targetValue;
            r.critical = false;
        }

        if ( r.hit ) {
            r.damage = Dice.roll(a.getDamage());
            if ( r.critical ) {
                r.damage +=  Dice.roll(a.getDamage());
            }

            target.hit(r.damage);
        }

        // save results
        metrics.attackDamage(attacker, target, r);
        attackResults.add(r);
    }

    public boolean finishRound() {
        int alive = 0;
        for(Participant p : participants) {
            outcome.add(p.toString());
            if ( p.getHitPoints() > 0 ) {
                alive++;
                if ( victor != null && p.getHitPoints() > victor.getHitPoints() ) {
                    victor = p;
                }
                p.incrementSurvived();
            }
        }
        metrics.finishRound(start, this);
        return alive > 1; // highlander. ;)
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Battle ").append(id).append(" round ").append(number);
        str.append("\nAttacks:\n");
        for( Result r : attackResults ) {
            str.append("   ").append(r).append("\n");
        }
        str.append("Outcome:\n");
        for( String o : outcome ) {
            str.append("   ").append(o).append("\n");
        }
        return str.toString();
    }

    static class Result {
        boolean hit;
        boolean critical;
        String attackerName;
        String attackName;
        String targetName;
        int damage;

        public String toString() {
            if ( hit ) {
                return String.format("%s%s attacked %s using %s for %d damage",
                    attackerName, critical ? " critically" : "", targetName, attackName, damage);
            } else {
                return String.format("%s attacked %s with %s, but%s misssed",
                    attackerName, targetName, attackName, critical ? " critically" : "");
            }
        }

        public boolean isCritical() {
            return critical;
        }
        public boolean isHit() {
            return hit;
        }

        public void setHit(boolean hit) {
            this.hit = hit;
        }

        public String getAttackerName() {
            return attackerName;
        }

        public void setAttackerName(String attackerName) {
            this.attackerName = attackerName;
        }

        public String getAttackName() {
            return attackName;
        }

        public void setAttackName(String attackName) {
            this.attackName = attackName;
        }

        public String getTargetName() {
            return targetName;
        }

        public void setTargetName(String targetName) {
            this.targetName = targetName;
        }

        public int getDamage() {
            return damage;
        }

        public void setDamage(int damage) {
            this.damage = damage;
        }
    }

}
