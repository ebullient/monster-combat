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
package application.monsters;

/**
 * Information about an attack a Monster can perform.
 * If they are proficient, there will be an additional abilityModifier.
 * The damage string is something like: 1d20+6
 * @see format for Bestiary from https://github.com/Cphrampus/DnDAppFiles
 */
public class Attack {
    String name;
    int abilityModifier;
    String damage;

    public Attack(String name, int abilityModifier, String damage) {
        this.name = name;
        this.abilityModifier = abilityModifier;
        this.damage = damage;
	}

	public String getName() {
        return name;
    }

    public int getAbilityModifier() {
        return abilityModifier;
    }

    public String getDamage() {
        return damage;
    }

    public String toString() {
        return String.format("[%s: Hit: %d, Damage: %s]",
            name, abilityModifier, damage);
    }
}
