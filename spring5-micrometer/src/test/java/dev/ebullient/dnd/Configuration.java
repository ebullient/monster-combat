package dev.ebullient.dnd;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import dev.ebullient.dnd.bestiary.Bestiary;
import dev.ebullient.dnd.bestiary.compendium.CompendiumReader;

@TestConfiguration
public class Configuration {
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
}
