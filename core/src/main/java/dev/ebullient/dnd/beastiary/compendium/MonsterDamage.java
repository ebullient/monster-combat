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

import java.util.List;

import dev.ebullient.dnd.combat.Attack;
import dev.ebullient.dnd.mechanics.Ability;

/**
 * POJO for damage from monster attack read from JSON
 *
 * @see CompendiumReader
 */
public class MonsterDamage implements Attack.Damage {
    String type;
    String amount;
    List<Ability> disadvantage;
    String savingThrow;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public List<Ability> getDisadvantage() {
        return disadvantage;
    }

    public void setDisadvantage(List<Ability> disadvantage) {
        this.disadvantage = disadvantage;
    }

    public String getSavingThrow() {
        return savingThrow;
    }

    public void setSavingThrow(String savingThrow) {
        this.savingThrow = savingThrow;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!amount.isEmpty()) {
            sb.append(amount).append("|");
        }
        sb.append(type);
        if (savingThrow != null) {
            sb.append("|").append(savingThrow);
        }
        if (disadvantage != null) {
            sb.append("|").append("-").append(disadvantage);
        }
        return sb.toString();
    }
}
