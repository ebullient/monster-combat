package dev.ebullient.dnd.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
class Event implements dev.ebullient.dnd.combat.RoundResult.Event {

    @JsonDeserialize(as = dev.ebullient.dnd.client.Combatant.class)
    Combatant actor;

    @JsonDeserialize(as = dev.ebullient.dnd.client.Combatant.class)
    Combatant target;

    String name;
    String type;
    String attack;

    String actorStartingCondition;
    String targetStartingCondition;
    String actorEndingCondition;
    String targetEndingCondition;

    boolean hit;
    boolean critical;
    boolean saved;
    int damageAmount;

    boolean effectSaved;
    int effectAmount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Combatant getActor() {
        return actor;
    }

    public void setActor(Combatant actor) {
        this.actor = actor;
    }

    public Combatant getTarget() {
        return target;
    }

    public void setTarget(Combatant target) {
        this.target = target;
    }

    public String getActorStartingCondition() {
        return actorStartingCondition;
    }

    public void setActorStartingCondition(String actorStartingCondition) {
        this.actorStartingCondition = actorStartingCondition;
    }

    public String getTargetStartingCondition() {
        return targetStartingCondition;
    }

    public void setTargetStartingCondition(String targetStartingCondition) {
        this.targetStartingCondition = targetStartingCondition;
    }

    public String getActorEndingCondition() {
        return actorEndingCondition;
    }

    public void setActorEndingCondition(String actorEndingCondition) {
        this.actorEndingCondition = actorEndingCondition;
    }

    public String getTargetEndingCondition() {
        return targetEndingCondition;
    }

    public void setTargetEndingCondition(String targetEndingCondition) {
        this.targetEndingCondition = targetEndingCondition;
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public boolean isCritical() {
        return critical;
    }

    public void setCritical(boolean critical) {
        this.critical = critical;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public int getDamageAmount() {
        return damageAmount;
    }

    public void setDamageAmount(int damageAmount) {
        this.damageAmount = damageAmount;
    }

    public boolean isEffectSaved() {
        return effectSaved;
    }

    public void setEffectSaved(boolean effectSaved) {
        this.effectSaved = effectSaved;
    }

    public int getEffectAmount() {
        return effectAmount;
    }

    public void setEffectAmount(int effectAmount) {
        this.effectAmount = effectAmount;
    }
}
