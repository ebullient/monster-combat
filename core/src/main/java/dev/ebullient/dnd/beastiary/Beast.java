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
package dev.ebullient.dnd.beastiary;

import java.util.List;
import java.util.Locale;

public interface Beast {

    enum Size {
        TINY,
        SMALL,
        MEDIUM,
        LARGE,
        HUGE,
        GARGANTUAN;
    }

    enum Type {
        ABERRATION,
        BEAST,
        CELESTIAL,
        CONSTRUCT,
        DRAGON,
        ELEMENTAL,
        FEY,
        FIEND,
        GIANT,
        HUMANOID,
        MONSTROSITY,
        OOZE,
        PLANT,
        SWARM,
        UNDEAD,
        OTHER;
    }

    enum Statistic {
        STR,
        DEX,
        CON,
        INT,
        WIS,
        CHA;

        public static Statistic convert(String score) {
            switch(score.toLowerCase(Locale.ROOT)) {
                case "strength": return STR;
                case "dexterity": return DEX;
                case "constitution": return CON;
                case "intelligence": return INT;
                case "wisdom": return WIS;
                case "charisma": return CHA;
            }
            return null;
        }
    }

    interface Damage {
        public String getType();
        public String getAmount();
        public String getSavingThrow();
    }

    interface Attack {
        String getName();
        List<? extends Damage> getDamage();
        int getAttackModifier();
    }

    interface Participant {
        String getName();
        String getDescription();

        int getInitiative();
        int getArmorClass();
        int getPassivePerception();
        int getModifier(Statistic s);
        int getAbility(Statistic s);
        int getSavingThrow(Statistic s);

        int getHitPoints();
        boolean isAlive();

        List<Attack> getAttacks();
        void takeDamage(int damage);
    }

    public float getCR();
    public Participant createParticipant();
}
