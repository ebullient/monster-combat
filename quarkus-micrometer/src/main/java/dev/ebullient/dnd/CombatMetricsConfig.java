package dev.ebullient.dnd;

import javax.enterprise.inject.Produces;

import dev.ebullient.dnd.combat.client.CombatMetrics;
import io.micrometer.core.instrument.MeterRegistry;

public class CombatMetricsConfig {

    @Produces
    public CombatMetrics createCombatMetrics(MeterRegistry registry) {
        return new CombatMetrics(registry);
    }
}
