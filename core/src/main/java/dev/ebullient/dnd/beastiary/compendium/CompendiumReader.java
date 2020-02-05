/*
 * Copyright © 2020 IBM Corp. All rights reserved.
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
package dev.ebullient.dnd.beastiary.compendium;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.ebullient.dnd.beastiary.Beastiary;

public class CompendiumReader {
    final static ObjectMapper mapper = new ObjectMapper();
    final static TypeReference<Map<String, Monster>> typeRef = new TypeReference<Map<String, Monster>>() {
    };

    public static void addToBeastiary(Beastiary beastiary) throws IOException {
        ClassPathResource resource = new ClassPathResource("compendium.json");

        try (InputStream fileStream = new FileInputStream(resource.getFile())) {
            Map<String, Monster> compendium = mapper.readValue(fileStream, typeRef);
            for (Monster m : compendium.values()) {
                beastiary.save(m.asBeast());
            }
        }
    }
}
