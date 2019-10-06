/*
 * Copyright © 2019 IBM Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package application;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import application.monsters.Beastiary;
import application.monsters.BeastiaryParser;

@Configuration
public class ApplicationConfig {
    static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

    @Autowired
    PrometheusScrapeEndpoint prometheusScrapeEndpoint;

    @Bean
    public Beastiary createBeastiary() {
        Beastiary beastiary = new Beastiary();
        try {
            new BeastiaryParser().parse(beastiary);
        } catch (IOException ioe) {
            logger.error("Exception occurred filling the beastiary", ioe);
        }
        return beastiary;
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
            request-> ok()
                .contentType(MediaType.TEXT_HTML)
                .syncBody(html)
        );
    }

    /**
     * Look for other resources as public files
     */
    @Bean
    public RouterFunction<ServerResponse> staticRouter() {
        return RouterFunctions.resources("/**", new ClassPathResource("public/"));
    }

    @Bean
    public RouterFunction<ServerResponse> metricsHack() {
        // The index.html resource is passed as a parameter.
        // This generates a routing function that returns the index.html resource when '/' is matched
        return RouterFunctions.route(
            GET("/metrics"),
            request-> ok()
                .syncBody(prometheusScrapeEndpoint.scrape())
        );
    }
}
