package dev.ebullient.dnd.combat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ebullient.dnd.mechanics.Dice;
import dev.ebullient.dnd.mechanics.Type;

class EncounterRoundResult implements RoundResult {
    static final Logger logger = LoggerFactory.getLogger(EncounterRoundResult.class);

    List<EncounterCombatant> initiativeOrder;
    List<EncounterCombatant> survivors;
    List<EncounterAttackEvent> events;

    final int numCombatants;
    final int numTypes;
    final int crDelta;
    final int sizeDelta;
    final EncounterTargetSelector selector;
    final Dice.Method method;
    final String encounterId;

    EncounterRoundResult(List<EncounterCombatant> initiativeOrder,
            EncounterTargetSelector selector, Dice.Method method, String encounterId) {

        // Remember details about this matchup
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
        this.initiativeOrder = initiativeOrder;
        this.survivors = new ArrayList<>(initiativeOrder);
        this.numCombatants = initiativeOrder.size();

        this.crDelta = maxCR - minCR;
        this.sizeDelta = maxSize - minSize;
        this.numTypes = types.size();
        this.selector = selector;
        this.encounterId = encounterId;
        this.method = method;
    }

    public List<EncounterAttackEvent> getEvents() {
        return events;
    }

    public List<EncounterCombatant> getSurvivors() {
        return survivors;
    }

    public int getNumCombatants() {
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
        return EncounterTargetSelector.targetSelectorToString(selector, numCombatants);
    }

    void go() {
        for (EncounterCombatant actor : initiativeOrder) {
            if (actor.isAlive()) {
                EncounterCombatant target = selector.chooseTarget(actor, initiativeOrder);

                // Single or many attacks
                List<Attack> attacks = actor.getAttacks();

                // A condition can impose a single attack constraint
                if (attacks.size() == 1 || actor.attackLimit()) {
                    makeAttack(actor, attacks.get(0), target);
                } else {
                    for (Attack a : attacks) {
                        makeAttack(actor, a, target);
                    }
                }

                // Highlander
                if (target.hitPoints <= 0) {
                    survivors.remove(target);
                }
            }
        }
    }

    void makeAttack(EncounterCombatant actor, Attack a, EncounterCombatant target) {
        if (target.isAlive()) {
            EncounterAttackEvent r = new EncounterAttackEvent(actor, target, a, method, encounterId);
            r.attack();
            events.add(r);

            logger.debug("attack: {}", r);
        }
    }
}
