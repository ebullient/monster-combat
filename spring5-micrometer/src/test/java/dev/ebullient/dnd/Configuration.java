/*
 * Copyright © 2019,2020 IBM Corp. All rights reserved.
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
package dev.ebullient.dnd;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import dev.ebullient.dnd.beastiary.Beastiary;
import dev.ebullient.dnd.beastiary.compendium.CompendiumReader;

@TestConfiguration
public class Configuration {
    static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

    @Bean
    public Beastiary createBeastiary() {
        Beastiary beastiary = new Beastiary();
        try {
            CompendiumReader.addToBeastiary(beastiary);
        } catch (IOException e) {
            logger.error("Exception occurred filling the beastiary", e);
        }
        return beastiary;
    }
}
