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
package dev.ebullient.dnd.combat;

import java.util.List;

public interface RoundResult {

    public interface Event {
        public String getName();

        public String getType();

        public Combatant getActor();

        public Combatant getTarget();

        public boolean isCritical();

        public boolean isHit();

        public boolean isSaved();

        public String hitOrMiss();

        public int getDamageAmount();

        public int getAttackModifier();

        public int getDifficultyClass();
    }

    public List<? extends Event> getEvents();

    public List<? extends Combatant> getSurvivors();

    public int getSize();

    public int getSizeDelta();

    public int getCrDelta();

    public int getNumTypes();

    public String getSelector();
}
