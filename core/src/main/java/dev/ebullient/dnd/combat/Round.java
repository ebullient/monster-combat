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
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import dev.ebullient.dnd.mechanics.Ability;
import dev.ebullient.dnd.mechanics.Dice;

public class Round {

    final List<Combatant> initiativeOrder;
    final TargetSelector selector;
    final List<AttackResult> whatHappened = new ArrayList<>();
    final Dice.Method method;

    public Round(List<Combatant> initiativeOrder, TargetSelector selector, Dice.Method method) {
        this.initiativeOrder = initiativeOrder;
        this.selector = selector;
        this.method = method;
    }

    public List<Combatant> takeTurns() {
        List<Combatant> survivors = new ArrayList<>(initiativeOrder);
        for (Combatant p : initiativeOrder) {
            if (p.isAlive()) {
                // TODO: multiple targets, not just multiple attacks
                Combatant target = selector.chooseTarget(p, initiativeOrder);

                // Single or many attacks
                List<Attack> attacks = p.getAttacks();
                for (Attack a : attacks) {
                    if (target.isAlive()) {
                        whatHappened.add(new AttackResult(p, target, a, method));
                    }
                }

                // Highlander
                if (!target.isAlive()) {
                    survivors.remove(p);
                }
            }
        }
        return survivors;
    }

    public List<Result> getWhatHappened() {
        return Collections.unmodifiableList(whatHappened);
    }

    static class AttackResult implements Result {
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

            attack();
        }

        Result attack() {
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
            if (hit) {
                return String.format("%s%s attacked %s for %d damage (%s)",
                        attacker.getName(), critical ? " critically" : "", target.getName(), damage, a.toString());
            } else {
                return String.format("%s attacked %s, but%s missed (%s)",
                        attacker.getName(), target.getName(), critical ? " critically" : "", a.toString());
            }
        }
    }
}
