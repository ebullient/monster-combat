/*******************************************************************************
 * Copyright (c) 2017,2018 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.gameontext.sample.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This is how our room is described.
 * - Update attributes dynamically in {@link RoomImplementation} as the room is used
 *
 * @see RoomImplementation
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class RoomDescription {

    @JsonProperty("type")
    private final String type = "location";

    private final Map<String, String> commands = new HashMap<>();
    private final Set<String> items = new HashSet<>();
    private String name = "defaultRoomNickName";
    private String fullName = "A room with the default fullName still set in the source";
    private String description = "A room that still has the default description set in the source";

    @JsonIgnore
    private volatile String cache = null;

    /**
     * @return The room's short name
     */
    public String getName() {
        return name;
    }

    /**
     * The name for a room should match the
     * name it was registered with.
     */
    public void setName(String name) {
        if (name != null) {
            this.name = name;
        }
    }

    /**
     * @return The room's long name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * The display name for a room can change at any time.
     *
     * @param fullName A new display name for the room
     */
    public void setFullName(String fullName) {
        if (fullName != null) {
            this.fullName = fullName;
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description != null) {
            this.description = description;
        }
    }

    /**
     * Custom commands are optional.
     */
    public Map<String, String> getCommands() {
        return commands;
    }

    public void addCommand(String command, String description) {
        if (description == null) {
            throw new IllegalArgumentException("description is required");
        }
        commands.put(command, description);
        cache = null;
    }

    public void removeCommand(String command) {
        commands.remove(command);
        cache = null;
    }

    /**
     * Room inventory objects are optional.
     */
    @JsonProperty("roomInventory")
    public Set<String> getInventory() {
        return items;
    }

    public void addItem(String itemName) {
        items.add(itemName);
        cache = null;
    }

    public void removeItem(String itemName) {
        items.remove(itemName);
        cache = null;
    }

    public String cachedValue() {
        return cache;
    }

    public void cache(String value) {
        cache = null;
    }

    @Override
    public String toString() {
        return  "type="+ type +
                ", name=" + name +
                ", fullName=" + fullName +
                ", description=" + description +
                ", commands=" + commands +
                ", items=" + items;
    }
}
