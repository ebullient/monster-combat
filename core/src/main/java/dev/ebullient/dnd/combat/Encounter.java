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
import java.util.Locale;
import java.util.regex.Matcher;

import dev.ebullient.dnd.mechanics.Ability;
import dev.ebullient.dnd.mechanics.Dice;

public class Encounter {

    final TargetSelector selector;
    final Dice.Method method;

    public Encounter(TargetSelector selector, Dice.Method method) {
        this.selector = selector;
        this.method = method;
    }

    public RoundResult takeTurns(List<Combatant> initiativeOrder) {
        RoundResult result = new RoundResult(initiativeOrder);
        for (Combatant p : initiativeOrder) {
            if (p.isAlive()) {
                // TODO: multiple targets, not just multiple attacks
                Combatant target = selector.chooseTarget(p, initiativeOrder);

                // Single or many attacks
                List<Attack> attacks = p.getAttacks();
                for (Attack a : attacks) {
                    if (target.isAlive()) {
                        AttackResult r = new AttackResult(p, target, a, method).attack();
                        result.events.add(r);
                    }
                }

                // Highlander
                if (!target.isAlive()) {
                    result.survivors.remove(p);
                }
            }
        }
        return result;
    }

    public static class RoundResult {
        List<Combatant> survivors;
        List<AttackResult> events;

        RoundResult(List<Combatant> initiativeOrder) {
            survivors = new ArrayList<>(initiativeOrder);
            events = new ArrayList<>();
        }

        public List<AttackResult> getEvents() {
            return events;
        }

        public List<Combatant> getSurvivors() {
            return survivors;
        }
    }


    public static class AttackResult {
        final Combatant attacker;
        final Combatant target;
        final Attack a;

        boolean hit;
        boolean critical;
        boolean saved;
        Dice.Method method;
        int damage;

        AttackResult(Combatant attacker, Combatant target, Attack a, Dice.Method method) {
            this.attacker = attacker;
            this.target = target;
            this.a = a;
            this.method = method;
        }

        AttackResult attack() {
            if (a.getAttackModifier() != 0) {
                attemptAttack();
            } else if (a.getSavingThrow() != null) {
                makeAttackWithSavingThrow();
            } else {
                // We're still reading from some input somewhere.
                throw new IllegalArgumentException(attacker.getName() + " has badly formed attack " + a);
            }
            return this;
        }

        void attemptAttack() {
            // Did we hit?
            int attackRoll = Dice.d20();
            if (attackRoll == 1) {
                // critical fail. WOOPS!
                critical = true;
                hit = false;
            } else if (attackRoll == 20) {
                // critical hit! double damage!
                critical = true;
                hit = true;
            } else {
                critical = false;
                // Add attack modifier, then see if we hit. ;)
                attackRoll += a.getAttackModifier();
                int targetValue = target.getArmorClass();
                hit = attackRoll >= targetValue;
            }

            if (hit) {
                damage = Dice.roll(a.getDamage().getAmount(), method);
                if (critical) {
                    damage += damage;
                }
                target.takeDamage(damage); // ouch
            }
        }

        void makeAttackWithSavingThrow() {
            hit = true;

            Matcher m = Attack.SAVE.matcher(a.getSavingThrow());
            if (m.matches()) {
                int dc = Integer.parseInt(m.group(2));
                int save = Dice.d20() + target.getSavingThrow(Ability.valueOf(m.group(1)));

                damage = Dice.roll(a.getDamage().getAmount(), method);

                if (save >= dc) {
                    saved = true;
                    damage = damage / 2;
                }

                target.takeDamage(damage); // ouch
            } else {
                throw new IllegalArgumentException(attacker.getName() + " has badly formed saving throw " + a);
            }
        }

        public String toString() {
            String success = hit ? "hit>" : "miss:";
            success = critical ? success.toUpperCase(Locale.ROOT) : success;

            StringBuilder sb = new StringBuilder();
            sb.append(success).append(" ")
                .append(attacker.getName()).append("(").append(attacker.getRelativeHealth()).append(")")
                .append(" -> ")
                .append(target.getName()).append("(").append(target.getRelativeHealth()).append(")");

            if ( damage != 0 ) {
                sb.append(" for ").append(damage).append(" damage using ").append(a);
            }

            return sb.toString();
        }

        public boolean wasCritical() {
            return critical;
        }

        public boolean wasHit() {
            return hit;
        }

        public boolean wasSaved() {
            return saved;
        }

        public int getDamage() {
            return damage;
        }
    }
}
