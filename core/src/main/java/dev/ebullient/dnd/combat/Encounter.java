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
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
    final List<EncounterCombatant> initiativeOrder;
    final int numCombatants;
    final int numTypes;
    final int crDelta;
    final int sizeDelta;

    Encounter(List<EncounterCombatant> combatants, TargetSelector selector, Dice.Method method) {
        this.initiativeOrder = new ArrayList<>(combatants);
        this.initiativeOrder.sort(Comparators.InitiativeOrder);
        this.numCombatants = initiativeOrder.size();
        this.selector = selector;
        this.method = method;

        EncounterCombatant first = initiativeOrder.iterator().next();
        int maxCR = first.beast.getCR();
        int minCR = maxCR;
        int maxSize = first.beast.getSize().ordinal();
        int minSize = maxSize;
        Set<Type> types = new HashSet<>();

        for (EncounterCombatant x : combatants) {
            types.add(x.beast.getType());
            maxCR = Math.max(x.beast.getCR(), maxCR);
            minCR = Math.min(x.beast.getCR(), minCR);
            maxSize = Math.max(x.beast.getSize().ordinal(), maxSize);
            minSize = Math.min(x.beast.getSize().ordinal(), minSize);
        }

        this.crDelta = maxCR - minCR;
        this.sizeDelta = maxSize - minSize;
        this.numTypes = types.size();
    }

    public boolean isFinal() {
        return initiativeOrder.size() <= 1;
    }

    public int getSize() {
        return numCombatants;
    }

    public int getSizeDelta() {
        return sizeDelta;
    }

    public int getCrDelta() {
        return crDelta;
    }

    public int getNumTypes() {
        return numTypes;
    }

    public String getSelector() {
        return selector.toString();
    }

    public RoundResult oneRound() {
        logger.debug("oneRound: {} {}", initiativeOrder, id);

        Result result = new Result(initiativeOrder, selector);

        for (EncounterCombatant actor : initiativeOrder) {
            if (actor.isAlive()) {
                EncounterCombatant target = selector.chooseTarget(actor, initiativeOrder);

                // Single or many attacks
                List<Attack> attacks = actor.getAttacks();

                // A condition can impose a single attack constraint
                if (attacks.size() == 1 || actor.attackLimit()) {
                    makeAttack(result, actor, attacks.get(0), target);
                } else {
                    for (Attack a : attacks) {
                        makeAttack(result, actor, a, target);
                    }
                }

                // Highlander
                if (target.hitPoints <= 0) {
                    result.survivors.remove(target);
                }
            }
        }

        initiativeOrder.retainAll(result.survivors);

        logger.debug("oneRound: survivors {} {}", result.survivors, id);
        return result;
    }

    void makeAttack(Result result, EncounterCombatant actor, Attack a, EncounterCombatant target) {
        if (target.isAlive()) {
            AttackEvent r = new AttackEvent(actor, target, a, method, id);
            r.attack();
            result.events.add(r);
            logger.debug("attack: {} {}", r, id);
        }
    }

    public static class Result implements RoundResult {
        List<EncounterCombatant> survivors;
        List<AttackEvent> events;
        final int numCombatants;
        final int numTypes;
        final int crDelta;
        final int sizeDelta;
        final String selector;

        Result(List<EncounterCombatant> initiativeOrder, TargetSelector selector) {
            EncounterCombatant first = initiativeOrder.iterator().next();
            int maxCR = first.beast.getCR();
            int minCR = maxCR;
            int maxSize = first.beast.getSize().ordinal();
            int minSize = maxSize;
            Set<Type> types = new HashSet<>();

            for (EncounterCombatant x : initiativeOrder) {
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
            this.selector = selector.toString();
        }

        public List<AttackEvent> getEvents() {
            return events;
        }

        public List<EncounterCombatant> getSurvivors() {
            return survivors;
        }

        public int getSize() {
            return numCombatants;
        }

        public int getSizeDelta() {
            return sizeDelta;
        }

        public int getCrDelta() {
            return crDelta;
        }

        public int getNumTypes() {
            return numTypes;
        }

        public String getSelector() {
            return selector;
        }
    }

    public static class AttackEvent implements RoundResult.Event {
        @JsonIgnore
        final String encounterId;

        @JsonIgnore
        final Dice.Method method;

        @JsonIgnore
        final Attack attack;

        final EncounterCombatant actor;
        final EncounterCombatant target;
        final EncounterCondition actorStartingCondition;
        final EncounterCondition targetStartingCondition;
        EncounterCondition actorEndingCondition;
        EncounterCondition targetEndingCondition;

        boolean hit;
        boolean critical;
        boolean saved;
        int damageAmount;

        int difficultyClass;
        int attackModifier;

        boolean effectSaved;
        int effectAmount;

        AttackEvent(EncounterCombatant actor, EncounterCombatant target, Attack attack, Dice.Method method,
                String encounterId) {
            this.actor = actor;
            this.target = target;
            this.attack = attack;
            this.method = method;
            this.encounterId = encounterId;

            // conditions change between rounds/attacks.
            // save what was present for this attack for poking and prodding later.
            this.actorStartingCondition = actor.condition;
            this.targetStartingCondition = target.condition;
        }

        public String getName() {
            return attack.getName();
        }

        public String getType() {
            return attack.getDamage().getType();
        }

        public EncounterCombatant getActor() {
            return actor;
        }

        public EncounterCombatant getTarget() {
            return target;
        }

        public boolean isCritical() {
            return critical;
        }

        public boolean isHit() {
            return hit;
        }

        public boolean isSaved() {
            return saved;
        }

        public int getDamageAmount() {
            return damageAmount;
        }

        public int getAttackModifier() {
            return attackModifier;
        }

        public int getDifficultyClass() {
            return difficultyClass;
        }

        public String getActorStartingCondition() {
            return actorStartingCondition == null ? "" : actorStartingCondition.toString();
        }

        public String getTargetStartingCondition() {
            return targetStartingCondition == null ? "" : targetStartingCondition.toString();
        }

        public String getActorEndingCondition() {
            return actorEndingCondition == null ? "" : actorEndingCondition.toString();
        }

        public String getTargetEndingCondition() {
            return targetEndingCondition == null ? "" : targetEndingCondition.toString();
        }

        AttackEvent attack() {
            if (attack.getSavingThrow() != null) {
                hit = true;
                makeActionWithSavingThrow(attack.getDamage(), false);
            } else {
                attemptMeleeAttack();
            }

            Damage effect = attack.getAdditionalEffect();
            if (effect != null) {
                makeActionWithSavingThrow(effect, true);
            }

            // Attacks / Actions can apply conditions.
            // Save what was present at the end of this attack for poking & prodding later.
            this.actorEndingCondition = actor.condition;
            this.targetEndingCondition = target.condition;
            return this;
        }

        /**
         * Actions that specify hit damage (+x to hit):
         *
         * Claws. Melee Weapon Attack: +7 to hit, reach 5 ft., one target.
         * Hit: 14 (2d8 + 5) slashing damage.
         *
         * Bites. Melee Weapon Attack: +2 to hit, reach 5 ft., one creature.
         * Hit: 17 (5d6) piercing damage.
         * Additional effect:
         * If the target is Medium or smaller, it must succeed on a DC 10 Strength saving throw
         * or be knocked prone. If the target is killed by this damage, it is absorbed into the mouther
         *
         * Life Drain. Melee Weapon Attack: +4 to hit, reach 5 ft., one creature.
         * Hit: 5 (1d6 + 2) necrotic damage.
         * Additional effect:
         * The target must succeed on a DC 13 Constitution saving throw or its
         * hit point maximum is reduced by an amount equal to the damage taken
         */
        void attemptMeleeAttack() {
            // Did we hit? Attack roll may be at advantage or disadvantage
            int attackRoll = Dice.d20(getRollConstraint());
            this.attackModifier = attack.getAttackModifier();
            this.difficultyClass = target.getArmorClass();

            if (attackRoll == 1) {
                // critical fail. WOOPS!
                this.critical = true;
                this.hit = false;
            } else if (attackRoll == 20) {
                // critical hit! double damage!
                this.critical = true;
                this.hit = true;
            } else {
                this.critical = false;

                // Add attack modifier, then see if we hit.
                attackRoll += this.attackModifier;
                this.hit = attackRoll >= this.difficultyClass;
            }

            if (hit) {
                Attack.Damage damage = attack.getDamage();

                String damageRoll = damage.getAmount();
                if (damageRoll != null && !damageRoll.isEmpty()) {
                    this.damageAmount = Dice.roll(damageRoll, method);
                    if (critical) {
                        damageAmount += damageAmount;
                    }

                    target.takeDamage(damageAmount); // ouch
                } else {
                    this.damageAmount = applyConditions(damage);
                    target.takeDamage(damageAmount); // ouch
                }
            }
        }

        /**
         * Attacks with a saving throw:
         *
         * Blinding Spittle (Recharge 5-6). The mouther spits a chemical glob at a
         * point it can see within 15 feet of it. The glob explodes in a blinding
         * flash of light on impact. Each creature within 5 feet of the flash must
         * succeed on a DC 13 Dexterity saving throw or be blinded until the
         * end of the mouther's next turn.
         *
         * Fire Breath (Recharge 5-6). The dragon exhales fire in a 30-foot cone.
         * Each creature in that area must make a DC 17 Dexterity saving throw,
         * taking 56 (16d6) fire damage on a failed save, or half as much
         * damage on a successful one.
         *
         * Or effect with saving throw:
         * If the target is Medium or smaller, it must succeed on a DC 10 Strength saving throw
         * or be knocked prone. If the target is killed by this damage, it is absorbed into the mouther
         */
        void makeActionWithSavingThrow(Damage damage, boolean additionalEffect) {
            boolean successful = false;
            int amount = 0;

            String throwStr = additionalEffect ? damage.getSavingThrow() : attack.getSavingThrow();

            Matcher m = Attack.SAVE.matcher(throwStr);
            if (m.matches()) {
                Ability ability = Ability.valueOf(m.group(1));
                int dc = Integer.parseInt(m.group(2));

                // A condition may require disadvantage on saving throws
                int savingThrow = Dice.d20(target.withConstraint(ability));
                int modifier = target.getSavingThrow(ability);

                if (savingThrow == 1) {
                    successful = false;
                } else if (savingThrow == 20) {
                    successful = true;
                } else {
                    // Add modifier, then see if target makes the saving throw
                    savingThrow += modifier;
                    successful = savingThrow >= dc;
                }

                String damageRoll = damage.getAmount();
                if (damageRoll != null && !damageRoll.isEmpty()) {
                    amount = Dice.roll(damageRoll, method);
                    if (successful) {
                        amount = amount / 2;
                    }
                    target.takeDamage(amount); // ouch
                } else if (!successful) {
                    amount = applyConditions(damage);
                    target.takeDamage(amount); // ouch
                }

                if (additionalEffect) {
                    this.effectSaved = successful;
                    this.effectAmount = amount;
                } else {
                    this.saved = successful;
                    this.damageAmount = amount;
                    this.difficultyClass = dc;
                    this.attackModifier = modifier;
                }
            }
        }

        int applyConditions(Attack.Damage d) {
            int damageAmount = 0;
            switch (d.getType()) {
                case "cursed":
                    // no damage, but disadvantage on later rolls
                    target.addCondition()
                            .setDisadvantage(d.getDisadvantage());
                    break;
                case "blinded":
                case "restrained":
                    target.addCondition()
                            .setTargetRollConstraint(Dice.Constraint.ADVANTAGE)
                            .setAttackRollConstraint(Dice.Constraint.DISADVANTAGE);
                    break;
                case "poisoned":
                case "frightened":
                    target.addCondition()
                            .setDisadvantage(Ability.allValues)
                            .setAttackRollConstraint(Dice.Constraint.DISADVANTAGE);
                    break;
                case "paralyzed":
                    target.addCondition()
                            .setDisadvantage(Ability.DEX, Ability.STR)
                            .setTargetRollConstraint(Dice.Constraint.ADVANTAGE)
                            .setAttackRollConstraint(Dice.Constraint.FAIL);
                    break;
                case "slowed":
                    target.addCondition()
                            .setSingleAttackLimit();
                    break;
                case "hpdrain":
                    target.addCondition()
                            .setMaxHitPointsDecrease(this.damageAmount);
                    break;
                default:
                    damageAmount = Dice.roll(d.getAmount(), method);
                    break;
            }
            return damageAmount;
        }

        Dice.Constraint getRollConstraint() {
            // A condition may give advantage to the attacker (a check on the target)
            Dice.Constraint constraint = target.getTargetConstraint();

            // Or, a condition may force a creature to attack with disadvantage
            if (constraint == Dice.Constraint.NONE) {
                constraint = actor.getAttackConstraint();
            }

            return constraint;
        }

        public String toString() {
            String success = hit ? "hit>" : "miss:";
            success = critical ? success.toUpperCase(Locale.ROOT) : success;

            StringBuilder sb = new StringBuilder();
            sb.append(success).append(" ")
                    .append(actor.getName()).append("(").append(actor.getRelativeHealth()).append(")")
                    .append(" -> ")
                    .append(target.getName()).append("(").append(target.getRelativeHealth()).append(")");

            if (damageAmount != 0) {
                sb.append(" for ").append(damageAmount).append(" damage using ").append(attack);
            }
            sb.append(" in ").append(encounterId);

            return sb.toString();
        }
    }

    /**
     * Make sure a beast satisfies combat requirements so we don't have to check
     * for bad/missing conditions during combat rounds (above).
     */
    public static void validate(Beast beast) {
        for (Attack a : beast.getAttacks()) {
            if (a == null) {
                throw new IllegalArgumentException(
                        String.format("Beast %s has a null element in list of attacks: %s",
                                beast.getName(), beast.getAttacks()));
            }

            if (a.getAttackModifier() == 0 && a.getSavingThrow() == null) {
                throw new IllegalArgumentException(
                        String.format("Beast %s attack %s does not specify an attack modifier or a saving throw: %s",
                                beast.getName(), a.getName(), a.getDescription()));
            }

            if (a.getAttackModifier() != 0 && a.getSavingThrow() != null) {
                throw new IllegalArgumentException(
                        String.format("Beast %s attack %s specifies both an attack modifier and a saving throw: %s",
                                beast.getName(), a.getName(), a.getDescription()));
            }

            String savingThrow = a.getSavingThrow();
            if (savingThrow != null) {
                Matcher m = Attack.SAVE.matcher(savingThrow);
                if (!m.matches()) {
                    throw new IllegalArgumentException(
                            String.format("Beast %s attack %s specifies an invalid saving throw (%s): %s",
                                    beast.getName(), a.getName(), savingThrow, a.getDescription()));
                }
            }

            Attack.Damage damage = a.getDamage();
            if (damage == null) {
                throw new IllegalArgumentException(
                        String.format("Beast %s attack %s does not specify damage: %s",
                                beast.getName(), a.getName(), a.getDescription()));
            }

            if (a.getAttackModifier() != 0 && damage.getType() == null) {
                throw new IllegalArgumentException(
                        String.format("Beast %s attack %s specifies an attack modifier but no hit damage: %s",
                                beast.getName(), a.getName(), a.getDescription()));
            }

            savingThrow = damage.getSavingThrow();
            if (savingThrow != null) {
                Matcher m = Attack.SAVE.matcher(savingThrow);
                if (!m.matches()) {
                    throw new IllegalArgumentException(
                            String.format("Beast %s attack %s specifies an invalid saving throw (%s): %s",
                                    beast.getName(), a.getName(), damage.getSavingThrow(), a.getDescription()));
                }
            }

        }
    }
}
