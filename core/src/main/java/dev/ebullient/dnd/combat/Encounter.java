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
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ebullient.dnd.beastiary.Beast;
import dev.ebullient.dnd.combat.Attack.Damage;
import dev.ebullient.dnd.mechanics.Ability;
import dev.ebullient.dnd.mechanics.Dice;
import dev.ebullient.dnd.mechanics.Type;

public class Encounter {
    static final Logger logger = LoggerFactory.getLogger(Encounter.class);
    final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");

    final String id = LocalDateTime.now().format(formatter) + "-" + Integer.toHexString(this.hashCode());

    final TargetSelector selector;
    final Dice.Method method;
    final Set<Combatant> initiativeOrder;
    final int numCombatants;
    final int numTypes;
    final int crDelta;
    final int sizeDelta;

    public Encounter(List<Beast> beasts, TargetSelector selector, Dice.Method method) {
        this(createSet(beasts, method), selector, method);
    }

    static Set<Combatant> createSet(List<Beast> beasts, Dice.Method method) {
        Set<Combatant> set = new TreeSet<>(Comparators.InitiativeOrder);
        for (Beast b : beasts) {
            set.add(new Combatant(b, method));
        }
        return set;
    }

    Encounter(Set<Combatant> combatants, TargetSelector selector, Dice.Method method) {
        Combatant first = combatants.iterator().next();
        int maxCR = first.beast.getCR();
        int minCR = maxCR;
        int maxSize = first.beast.getSize().ordinal();
        int minSize = maxSize;
        Set<Type> types = new HashSet<>();

        for (Combatant x : combatants) {
            types.add(x.beast.getType());
            maxCR = Math.max(x.beast.getCR(), maxCR);
            minCR = Math.min(x.beast.getCR(), minCR);
            maxSize = Math.max(x.beast.getSize().ordinal(), maxSize);
            minSize = Math.min(x.beast.getSize().ordinal(), minSize);
        }

        this.selector = selector;
        this.method = method;
        this.initiativeOrder = combatants;
        this.numCombatants = initiativeOrder.size();
        this.crDelta = maxCR - minCR;
        this.sizeDelta = maxSize - minSize;
        this.numTypes = types.size();
    }

    public boolean isFinal() {
        return initiativeOrder.size() <= 1;
    }

    public int size() {
        return numCombatants;
    }

    public int sizeDelta() {
        return sizeDelta;
    }

    public int crDelta() {
        return crDelta;
    }

    public int numTypes() {
        return numTypes;
    }

    public RoundResult oneRound() {
        logger.debug("oneRound: {}", initiativeOrder);

        RoundResult result = new RoundResult(initiativeOrder);

        for (Combatant attacker : initiativeOrder) {
            if (attacker.isAlive()) {
                Combatant target = selector.chooseTarget(attacker, initiativeOrder);

                // Single or many attacks
                List<Attack> attacks = attacker.getAttacks();

                // A condition can impose a single attack constraint
                if (attacks.size() == 1 || attacker.attackLimit()) {
                    makeAttack(result, attacker, attacks.get(0), target);
                } else {
                    for (Attack a : attacks) {
                        makeAttack(result, attacker, a, target);
                    }
                }

                // Highlander
                if (target.hitPoints <= 0) {
                    result.survivors.remove(target);
                }
            }
        }

        initiativeOrder.retainAll(result.survivors);

        logger.debug("oneRound: survivors {}", result.survivors);
        return result;
    }

    void makeAttack(RoundResult result, Combatant attacker, Attack a, Combatant target) {
        if (a == null) {
            throw new IllegalStateException("Attack should not be null: " + attacker.getAttacks());
        }
        if (target.isAlive()) {
            AttackResult r = new AttackResult(attacker, target, a, method, id);
            r.attack();
            result.events.add(r);
            logger.debug("makeAttack: {}", r);
        }
    }

    public static class RoundResult {
        List<Combatant> survivors;
        List<AttackResult> events;
        final int numCombatants;
        final int numTypes;
        final int crDelta;
        final int sizeDelta;

        RoundResult(Set<Combatant> initiativeOrder) {
            Combatant first = initiativeOrder.iterator().next();
            int maxCR = first.beast.getCR();
            int minCR = maxCR;
            int maxSize = first.beast.getSize().ordinal();
            int minSize = maxSize;
            Set<Type> types = new HashSet<>();

            for (Combatant x : initiativeOrder) {
                types.add(x.beast.getType());
                maxCR = Math.max(x.beast.getCR(), maxCR);
                minCR = Math.min(x.beast.getCR(), minCR);
                maxSize = Math.max(x.beast.getSize().ordinal(), maxSize);
                minSize = Math.min(x.beast.getSize().ordinal(), minSize);
            }

            this.events = new ArrayList<>();
            this.survivors = new ArrayList<>(initiativeOrder);
            this.numCombatants = initiativeOrder.size();
            this.crDelta = maxCR - minCR;
            this.sizeDelta = maxSize - minSize;
            this.numTypes = types.size();
        }

        public List<AttackResult> getEvents() {
            return events;
        }

        public List<Combatant> getSurvivors() {
            return survivors;
        }

        public int size() {
            return numCombatants;
        }

        public int sizeDelta() {
            return sizeDelta;
        }

        public int crDelta() {
            return crDelta;
        }

        public int numTypes() {
            return numTypes;
        }
    }

    public static class AttackResult {
        final Combatant attacker;
        final Combatant target;
        final Attack a;
        final String encounterId;
        final Condition attackerStartingCondition;
        final Condition targetStartingCondition;
        Condition attackerEndingCondition;
        Condition targetEndingCondition;

        boolean hit;
        boolean critical;
        boolean saved;
        Dice.Method method;
        int damage;

        AttackResult(Combatant attacker, Combatant target, Attack a, Dice.Method method, String encounterId) {
            this.attacker = attacker;
            this.target = target;
            this.a = a;
            this.method = method;
            this.encounterId = encounterId;

            // conditions change between rounds/attacks.
            // save what was present for this attack for poking and prodding later.
            this.attackerStartingCondition = attacker.condition;
            this.targetStartingCondition = target.condition;
        }

        public String getName() {
            return a.getName();
        }

        public String getType() {
            return a.getDamage().getType();
        }

        public Combatant getAttacker() {
            return attacker;
        }

        public Combatant getTarget() {
            return target;
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

        AttackResult attack() {
            if (a.getAttackModifier() != 0) {
                attemptAttack();
            } else if (a.getSavingThrow() != null) {
                makeAttackWithSavingThrow();
            } else {
                // We're still reading from some input somewhere.
                throw new IllegalArgumentException(attacker.getName() + " has badly formed attack " + a);
            }

            // Attacks / Actions can apply conditions.
            // Save what was present at the end of this attack for poking & prodding later.
            this.attackerEndingCondition = attacker.condition;
            this.targetEndingCondition = target.condition;
            return this;
        }

        void attemptAttack() {
            // Did we hit?
            int attackRoll;

            // attack roll may be at advantage or disadvantage
            attackRoll = Dice.d20(getRollConstraint());

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
                Attack.Damage d = a.getDamage();

                String amount = d.getAmount();
                if (amount == null || amount.isEmpty()) {
                    applyConditions(d, target);
                } else {
                    damage = Dice.roll(a.getDamage().getAmount(), method);
                    if (critical) {
                        damage += damage;
                    }
                    target.takeDamage(damage); // ouch
                }

                additionalEffects();
            }
        }

        void makeAttackWithSavingThrow() {
            hit = true;

            Matcher m = Attack.SAVE.matcher(a.getSavingThrow());
            if (m.matches()) {
                int dc = Integer.parseInt(m.group(2));
                int save;

                Ability ability = Ability.valueOf(m.group(1));

                // A condition may require disadvantage on saving throws
                save = Dice.d20(target.withConstraint(ability))
                        + target.getSavingThrow(ability);

                String amount = a.getDamage().getAmount();
                if (amount == null || amount.isEmpty()) {
                    applyConditions(a.getDamage(), target);
                } else {
                    damage = Dice.roll(amount, method);
                    if (save >= dc) {
                        saved = true;
                        damage = damage / 2;
                    }
                    target.takeDamage(damage); // ouch
                }
            } else {
                throw new IllegalArgumentException(attacker.getName() + " has badly formed saving throw " + a);
            }
        }

        void applyConditions(Attack.Damage d, Combatant target) {
            switch (d.getType()) {
                case "cursed":
                    // no damage, but disadvantage on later rolls
                    target.addCondition()
                            .disadvantage(d.getDisadvantage());
                    break;
                case "blinded":
                case "restrained":
                    target.addCondition()
                            .asTarget(Dice.Constraint.ADVANTAGE)
                            .onAttack(Dice.Constraint.DISADVANTAGE);
                    break;
                case "poisoned":
                case "frightened":
                    target.addCondition()
                            .disadvantage(Ability.allValues)
                            .onAttack(Dice.Constraint.DISADVANTAGE);
                    break;
                case "paralyzed":
                    target.addCondition()
                            .disadvantage(Ability.DEX, Ability.STR)
                            .asTarget(Dice.Constraint.ADVANTAGE)
                            .onAttack(Dice.Constraint.FAIL);
                    break;
                case "slowed":
                    target.addCondition()
                            .singleAttack();
                    break;
                default:
                    damage = Dice.roll(a.getDamage().getAmount(), method);
                    if (critical) {
                        damage += damage;
                    }
                    target.takeDamage(damage); // ouch
                    break;
            }
        }

        void additionalEffects() {
            // If the attack brings additional damage, and it hits..
            Damage effect = a.getAdditionalEffect();
            if (effect != null) {
                int effectDamage = 0;
                if (effect.getAmount() != null && !effect.getAmount().isEmpty()) {
                    effectDamage = Dice.roll(effect.getAmount(), method);
                }

                String savingThrow = effect.getSavingThrow();
                if (savingThrow != null) {
                    Matcher m = Attack.SAVE.matcher(savingThrow);
                    if (m.matches()) {
                        int dc = Integer.parseInt(m.group(2));
                        int save = Dice.d20() + target.getSavingThrow(Ability.valueOf(m.group(1)));

                        if ("hpdrain".equals(effect.getType())) {
                            if (save < dc) {
                                int maxhp = target.getMaxHitPoints();
                                if (damage > maxhp) {
                                    target.takeDamage(damage);

                                }
                            }
                        } else {
                            if (save >= dc) {
                                saved = true;
                                effectDamage = effectDamage / 2;
                            }
                            target.takeDamage(effectDamage); // ouch
                        }
                    }
                } else {
                    target.takeDamage(effectDamage); // ouch
                }
            }
        }

        Dice.Constraint getRollConstraint() {
            // A condition may give advantage to the attacker (a check on the target)
            Dice.Constraint c = target.rollAsTarget();

            // A condition may similarly require a creature to attack with disadvantage
            if (c == Dice.Constraint.NONE) {
                c = attacker.rollOnAttack();
            }

            return c;
        }

        public String toString() {
            String success = hit ? "hit>" : "miss:";
            success = critical ? success.toUpperCase(Locale.ROOT) : success;

            StringBuilder sb = new StringBuilder();
            sb.append(success).append(" ")
                    .append(attacker.getName()).append("(").append(attacker.getRelativeHealth()).append(")")
                    .append(" -> ")
                    .append(target.getName()).append("(").append(target.getRelativeHealth()).append(")");

            if (damage != 0) {
                sb.append(" for ").append(damage).append(" damage using ").append(a);
            }

            return sb.toString();
        }
    }
}
