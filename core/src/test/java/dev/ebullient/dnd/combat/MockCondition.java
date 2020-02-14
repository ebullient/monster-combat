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

import dev.ebullient.dnd.mechanics.Ability;
import dev.ebullient.dnd.mechanics.Dice;
import dev.ebullient.dnd.mechanics.Dice.Constraint;

public class MockCondition extends Condition {

    Dice.Constraint useConstraint;

    MockCondition(Dice.Constraint useConstraint) {
        this.useConstraint = useConstraint;
    }

    @Override
    Constraint getAbilityCheckConstraint(Ability ability) {
        return useConstraint;
    }

    @Override
    Constraint getAttackConstraint() {
        return useConstraint;
    }

    @Override
    Constraint getTargetConstraint() {
        return useConstraint;
    }

}
