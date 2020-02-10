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
package dev.ebullient.dnd.mechanics;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public enum Ability {
    STR,
    DEX,
    CON,
    INT,
    WIS,
    CHA;

    public static final List<Ability> allValues = Collections.unmodifiableList(Arrays.asList(values()));

    public static class All {
        Map<Ability, Integer> all = new HashMap<>();

        public void set(Ability a, int value) {
            all.put(a, value);
        }

        public void set(String a, int value) {
            all.put(convert(a), value);
        }

        public int get(Ability a) {
            Integer value = all.get(a);
            if (value == null) {
                return 0;
            }
            return value;
        }
    }

    public static Ability convert(String score) {
        switch (score.toLowerCase(Locale.ROOT)) {
            case "strength":
            case "str":
                return STR;
            case "dexterity":
            case "dex":
                return DEX;
            case "constitution":
            case "con":
                return CON;
            case "intelligence":
            case "int":
                return INT;
            case "wisdom":
            case "wis":
                return WIS;
            case "charisma":
            case "cha":
                return CHA;
        }

        throw new IllegalArgumentException("Unknown ability score: " + score);
    }
}
