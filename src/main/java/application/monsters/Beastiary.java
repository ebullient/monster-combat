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

import java.util.ArrayList;

import application.mechanics.Dice;

public class Beastiary {
    private ArrayList<Monster> monsters = new ArrayList<>();

    public void addMonster(Monster monster) {
        monsters.add(monster);
    }

    public Monster getRandomMonster() {
        return monsters.get(Dice.range(monsters.size()));
    }

    public String toString() {
        return new StringBuilder()
            .append("Monster bestiary containing ")
            .append(monsters.size())
            .append(" monsters")
            .toString();
    }
}
