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
package dev.ebullient.dnd.bestiary.compendium;

import dev.ebullient.dnd.combat.Attack;

/**
 * POJO for monster attack sread from JSON
 *
 * @see CompendiumReader
 */
public class MonsterAttack implements Attack {

    String name;
    boolean melee;
    int attackModifier;
    String savingThrow;
    String description;
    MonsterDamage damage;
    MonsterDamage additionalEffect;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAttackModifier() {
        return attackModifier;
    }

    public void setAttackModifier(int attackModifier) {
        this.attackModifier = attackModifier;
    }

    public MonsterDamage getDamage() {
        return damage;
    }

    public void setDamage(MonsterDamage damage) {
        this.damage = damage;
    }

    public String getSavingThrow() {
        return savingThrow;
    }

    public void setSavingThrow(String savingThrow) {
        this.savingThrow = savingThrow;
    }

    public boolean isMelee() {
        return melee;
    }

    public void setMelee(boolean melee) {
        this.melee = melee;
    }

    public MonsterDamage getAdditionalEffect() {
        return additionalEffect;
    }

    public void setAdditionalEffect(MonsterDamage additionalEffect) {
        this.additionalEffect = additionalEffect;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("[");
        if (attackModifier != 0) {
            sb.append(attackModifier).append("hit,");
        }
        if (savingThrow != null) {
            sb.append(savingThrow).append(",");
        }

        sb.append(damage.toString().replaceAll("\\s+", ""))
                .append("]");

        if (additionalEffect != null) {
            sb.append(additionalEffect.toString()
                    .replaceAll("\\s+", ""))
                    .append("]");
        }

        return sb.toString();
    }
}
