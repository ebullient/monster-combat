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

import java.util.Locale;

public enum Ability {
    STR,
    DEX,
    CON,
    INT,
    WIS,
    CHA;

    /**
     * Convenience structure for internal storage of
     * ability score, ability modifier or saving throws
     */
    public static class All {
        public int strength;
        public int dexterity;
        public int constitution;
        public int intelligence;
        public int wisdom;
        public int charisma;
    }

    public static Ability convert(String score) {
        switch (score.toLowerCase(Locale.ROOT)) {
            case "strength":
                return STR;
            case "dexterity":
                return DEX;
            case "constitution":
                return CON;
            case "intelligence":
                return INT;
            case "wisdom":
                return WIS;
            case "charisma":
                return CHA;
        }
        return null;
    }
}
