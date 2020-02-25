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
package dev.ebullient.dnd.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RoundResult implements dev.ebullient.dnd.combat.RoundResult {

    @JsonDeserialize(contentAs = dev.ebullient.dnd.client.Combatant.class)
    List<Combatant> survivors;

    @JsonDeserialize(contentAs = dev.ebullient.dnd.client.AttackEvent.class)
    List<Event> events;

    int numCombatants;
    int numTypes;
    int crDelta;
    int sizeDelta;
    String selector;

    public List<Combatant> getSurvivors() {
        return survivors;
    }

    public void setSurvivors(List<Combatant> survivors) {
        this.survivors = survivors;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public int getNumCombatants() {
        return numCombatants;
    }

    public void setNumCombatants(int numCombatants) {
        this.numCombatants = numCombatants;
    }

    public int getNumTypes() {
        return numTypes;
    }

    public void setNumTypes(int numTypes) {
        this.numTypes = numTypes;
    }

    public int getCrDelta() {
        return crDelta;
    }

    public void setCrDelta(int crDelta) {
        this.crDelta = crDelta;
    }

    public int getSizeDelta() {
        return sizeDelta;
    }

    public void setSizeDelta(int sizeDelta) {
        this.sizeDelta = sizeDelta;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }
}
