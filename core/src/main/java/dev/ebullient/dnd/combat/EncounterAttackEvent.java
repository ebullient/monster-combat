package dev.ebullient.dnd.combat;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

import dev.ebullient.dnd.combat.Attack.Damage;
import dev.ebullient.dnd.mechanics.Ability;
import dev.ebullient.dnd.mechanics.Dice;

class EncounterAttackEvent implements RoundResult.Event {

    final String encounterId;
    final Dice.Method method;
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
    boolean spellAttack = false;
    int damageAmount;

    int difficultyClass;
    int attackModifier;

    EncounterAttackEvent(EncounterCombatant actor, EncounterCombatant target, Attack attack, Dice.Method method,
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

    // Used for additional effects: attack w/ two results
    EncounterAttackEvent(EncounterAttackEvent that) {
        this.method = that.method;

        this.actor = that.actor;
        this.target = that.target;
        this.attack = that.attack;
        this.encounterId = that.encounterId;

        this.actorStartingCondition = that.actorStartingCondition;
        this.targetStartingCondition = that.targetStartingCondition;
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

    public boolean isSpellAttack() {
        return spellAttack;
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

    List<EncounterAttackEvent> attack() {
        EncounterAttackEvent additionalEvent = null;
        if (attack.getSavingThrow() != null) {
            makeActionWithSavingThrow(attack.getSavingThrow(), attack.getDamage());
        } else {
            attemptMeleeAttack();
        }

        Damage effect = attack.getAdditionalEffect();
        if (hit && effect != null) {
            additionalEvent = new EncounterAttackEvent(this);
            additionalEvent.makeActionWithSavingThrow(effect.getSavingThrow(), effect);
        }

        // Attacks / Actions can apply conditions.
        // Save what was present at the end of this attack for poking & prodding later.
        this.actorEndingCondition = actor.condition;
        this.targetEndingCondition = target.condition;
        if (additionalEvent == null) {
            return Arrays.asList(this);
        } else {
            return Arrays.asList(this, additionalEvent);
        }
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
    void makeActionWithSavingThrow(String throwStr, Damage damage) {
        this.hit = true;
        this.spellAttack = true;

        boolean successful = false;
        int amount = 0;

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

            this.saved = successful;
            this.damageAmount = amount;
            this.difficultyClass = dc;
            this.attackModifier = modifier;
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
