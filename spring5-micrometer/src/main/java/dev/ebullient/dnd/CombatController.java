package dev.ebullient.dnd;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.ebullient.dnd.bestiary.Bestiary;
import dev.ebullient.dnd.combat.Encounter;
import dev.ebullient.dnd.combat.RoundResult;
import dev.ebullient.dnd.combat.TargetSelector;
import dev.ebullient.dnd.combat.client.CombatMetrics;
import dev.ebullient.dnd.mechanics.Dice;
import dev.ebullient.dnd.mechanics.SecureRandomDice;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping(path = "/combat", produces = "application/json")
public class CombatController {
    static final Logger logger = LoggerFactory.getLogger(CombatController.class);

    final Bestiary beastiary;
    final CombatMetrics metrics;

    public CombatController(Bestiary beastiary, CombatMetrics metrics) {
        this.beastiary = beastiary;
        this.metrics = metrics;
        logger.debug("Controller initialized bestiary={}, metrics={}", this.beastiary, this.metrics);

        // Use secure random for dice rolling
        Dice.setRandomDice(new SecureRandomDice());
    }

    @GetMapping(path = "/any")
    Publisher<RoundResult> any() {
        return go(Dice.range(5) + 2);
    }

    @GetMapping(path = "/faceoff")
    Publisher<RoundResult> faceoff() {
        return go(2);
    }

    @GetMapping(path = "/melee")
    Publisher<RoundResult> melee() {
        return go(Dice.range(4) + 3);
    }

    Publisher<RoundResult> go(int howMany) {

        Encounter encounter = beastiary.buildEncounter()
                .setHowMany(howMany)
                .setTargetSelector(pickOne(howMany))
                .build();

        return Flux.push(emitter -> {
            int totalRounds = 0;

            while (!encounter.isFinal()) {
                totalRounds++;
                RoundResult result = encounter.oneRound();
                metrics.endRound(result);

                emitter.next(result);
            }

            emitter.complete();
            metrics.endEncounter(encounter, totalRounds);
        });
    }

    TargetSelector pickOne(int howMany) {
        int which = Dice.range(5);
        switch (which) {
            case 4:
                return TargetSelector.SelectBiggest;
            case 3:
                return TargetSelector.SelectSmallest;
            case 2:
                return TargetSelector.SelectByHighestRelativeHealth;
            case 1:
                return TargetSelector.SelectByLowestRelativeHealth;
            default:
            case 0:
                return TargetSelector.SelectAtRandom;
        }
    }
}
