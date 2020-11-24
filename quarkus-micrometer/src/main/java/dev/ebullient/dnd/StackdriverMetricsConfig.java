package dev.ebullient.dnd;

import javax.enterprise.inject.Produces;

import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.stackdriver.StackdriverMeterRegistry;
import io.quarkus.micrometer.runtime.MeterFilterConstraint;

public class StackdriverMetricsConfig {

    @Produces
    @MeterFilterConstraint(applyTo = StackdriverMeterRegistry.class)
    public MeterFilter stackDriverFilter() {
        return MeterFilter.commonTags(Tags.of("application", "quarkus-micrometer"));
    }
}
