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
package dev.ebullient.dnd.bestiary;

import java.util.List;

import dev.ebullient.dnd.combat.Attack;
import dev.ebullient.dnd.mechanics.Ability;
import dev.ebullient.dnd.mechanics.Size;
import dev.ebullient.dnd.mechanics.Type;

public interface Beast {

    String getName();

    String getHitPoints();

    String getChallengeRating();

    int getArmorClass();

    int getPassivePerception();

    int getAbilityModifier(Ability s);

    int getSavingThrow(Ability s);

    int getCR();

    Size getSize();

    Type getType();

    List<Attack> getAttacks();
}
