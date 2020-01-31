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
package dev.ebullient.dnd.beastiary;


public class Beastiary {

    long totalCount;

    /**
     * Add one beast to the Beastiary
     */
    public Beast save(Beast b) {
        return b;
    }

    /**
     * @return a random monster
     */
    public Beast findOne() {
        return null;
    }

    /**
     * @return a random beast with the requested challenge rating
     */
    public Beast findOneByChallengeRating(String cr) {
        return null;
    }


    public String toString() {
        return new StringBuilder()
            .append("Beastiary contains ")
            .append(totalCount)
            .append(" beasts")
            .toString();
    }
}
