package dev.ebullient.dnd;

import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Tag;
import org.eclipse.microprofile.metrics.annotation.RegistryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ebullient.dnd.combat.Encounter;
import dev.ebullient.dnd.combat.RoundResult;
import dev.ebullient.dnd.mechanics.Dice;

@ApplicationScoped
public class CombatMetrics {
    static final Logger logger = LoggerFactory.getLogger(dev.ebullient.dnd.combat.client.CombatMetrics.class);

    final MetricRegistry registry;
    final AtomicInteger last_roll = new AtomicInteger(0);
    final AtomicInteger last_felled = new AtomicInteger(0);

    public CombatMetrics(@RegistryType(type = MetricRegistry.Type.APPLICATION) MetricRegistry registry) {
        this.registry = registry;

        registry.register("last.felled", (Gauge<AtomicInteger>) () -> last_felled);
        registry.register("last.roll", (Gauge<AtomicInteger>) () -> last_roll);

        Dice.setMonitor((k, v) -> {
            registry.counter("dice.rolls", new Tag("die", k), new Tag("face", label(v))).inc();
            last_roll.set(v);
        });

        logger.debug("Created CombatMetrics with MeterRegistry: {}", registry);
    }

    public void endEncounter(Encounter e, int totalRounds) {
        registry.histogram("encounter.rounds",
                new Tag("numCombatants", label(e.getNumCombatants())),
                new Tag("targetSelector", e.getSelector()),
                new Tag("sizeDelta", label(e.getSizeDelta())))
                .update(totalRounds);
    }

    public void endRound(RoundResult result) {
        for (RoundResult.Event event : result.getEvents()) {
            registry.histogram("round.attacks",
                    new Tag("hitOrMiss", event.hitOrMiss()),
                    new Tag("attackType", event.getAttackType()),
                    new Tag("damageType", event.getType()),
                    new Tag("targetSelector", result.getSelector()))
                    .update(event.getDamageAmount());

            registry.histogram("attacker.damage",
                    new Tag("attacker", event.getActor().getName()),
                    new Tag("attackName", event.getName()),
                    new Tag("hitOrMiss", event.hitOrMiss()))
                    .update(event.getDamageAmount());

            registry.histogram("attack.success",
                    new Tag("attackType", event.getAttackType()),
                    new Tag("hitOrMiss", event.hitOrMiss()))
                    .update(event.getDifficultyClass());
        }

        last_felled.set(result.getNumCombatants() - result.getSurvivors().size());
    }

    String label(int value) {
        return String.format("%02d", value);
    }
}
