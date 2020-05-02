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
package dev.ebullient.dnd.mechanics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ChallengeRatingTest {

    @Test
    public void testCalculateCR() {
        int lowest = 0;
        int highest = 0;
        for (Type t : Type.allValues) {
            for (Size s : Size.allValues) {
                int cr = ChallengeRating.calculateCR(s, t);
                //System.out.println(s + " " + t + ": " + cr);

                if (cr < lowest) {
                    lowest = cr;
                } else if (cr > highest) {
                    highest = cr;
                }
            }
        }
        Assertions.assertEquals(-3, lowest, "Lowest possible value should be -3 (CR 1/8)");
        Assertions.assertTrue(highest < 23, "Highest value should be < 23");
    }
}
