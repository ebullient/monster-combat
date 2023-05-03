package dev.ebullient.dnd;

import dev.ebullient.dnd.combat.client.CombatMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.inject.Produces;

public class CombatMetricsConfig {

    @Produces
    public CombatMetrics createCombatMetrics(MeterRegistry registry) {
        return new CombatMetrics(registry);
    }
}
