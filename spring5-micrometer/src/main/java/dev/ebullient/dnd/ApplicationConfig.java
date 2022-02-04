package dev.ebullient.dnd;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import dev.ebullient.dnd.bestiary.Bestiary;
import dev.ebullient.dnd.bestiary.compendium.CompendiumReader;
import dev.ebullient.dnd.combat.client.CombatMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;

@Configuration
public class ApplicationConfig {
    static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

    @Bean
    public Bestiary createBestiary() {
        Bestiary beastiary = new Bestiary();
        try {
            CompendiumReader.addToBestiary(beastiary);
        } catch (IOException e) {
            logger.error("Exception occurred filling the beastiary", e);
        }
        return beastiary;
    }

    @Bean
    public CombatMetrics initCombatMetrics(MeterRegistry registry) {
        return new CombatMetrics(registry);
    }

    @Bean
    public MeterFilter configureCommonDistributionMetrics() {
        return new MeterFilter() {
            // public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
            //     if (id.getName().startsWith("encounter.rounds")) {
            //         return DistributionStatisticConfig.builder()
            //                 .minimumExpectedValue((long) 1)
            //                 .maximumExpectedValue((long) 15)
            //                 .build()
            //                 .merge(config);
            //     }
            //     return config;
            // }
        };
    }

    /**
     * Create a route for '/' to return index.html
     */
    @Bean
    public RouterFunction<ServerResponse> indexRouter(@Value("classpath:/public/index.html") Resource html) {
        // The index.html resource is passed as a parameter.
        // This generates a routing function that returns the index.html resource when '/' is matched
        return RouterFunctions.route(
                GET("/"),
                request -> ok()
                        .contentType(MediaType.TEXT_HTML)
                        .bodyValue(html));
    }

    /**
     * Look for other resources as public files
     */
    @Bean
    public RouterFunction<ServerResponse> staticRouter() {
        return RouterFunctions.resources("/**", new ClassPathResource("public/"));
    }
}
