package dev.ebullient.dnd;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/prometheus")
public class PrometheusResource {
    static final Logger logger = LoggerFactory.getLogger(PrometheusResource.class);

    final CombatMetrics combatMetrics;

    PrometheusResource(CombatMetrics combatMetrics) {
        this.combatMetrics = combatMetrics;

        logger.debug("Created PrometheusResource with: {}", combatMetrics);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String scrape() {
        return combatMetrics.scrape();
    }
}
