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
package dev.ebullient.dnd;

import java.util.List;

import dev.ebullient.dnd.combat.Attack;
import dev.ebullient.dnd.mechanics.Ability;

public class MockDamage implements Attack.Damage {

    public String type;
    public String amount;
    public String savingThrow;
    public List<Ability> disadvantage;
    public int duration;

    public MockDamage(String type, String amount) {
        this.type = type;
        this.amount = amount;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getAmount() {
        return amount;
    }

    @Override
    public List<Ability> getDisadvantage() {
        return disadvantage;
    }

    @Override
    public String getSavingThrow() {
        return savingThrow;
    }

    public int getDuration() {
        return duration;
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
