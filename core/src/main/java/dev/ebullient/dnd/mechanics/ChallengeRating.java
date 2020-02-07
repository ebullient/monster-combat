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
package dev.ebullient.dnd.mechanics;

public interface ChallengeRating {

    public static int stringToCr(String rating) {
        switch (rating) {
            case "0":
                return -3;
            case "1/8":
                return -2;
            case "1/4":
                return -1;
            case "1/2":
                return 0;
            default:
                return Integer.parseInt(rating);
        }
    }

    /**
     * Monsters are grouped by CR as a string/pretty value,
     * rather than stored as a double
     */
    public static String crToString(int cr) {
        switch (cr) {
            case -3:
                return "0";
            case -2:
                return "1/8";
            case -1:
                return "1/4";
            case 0:
                return "1/2";
            default:
                return Integer.toString(cr);
        }
    }

    /**
     * Invent a challenge rating that takes 2x size, and munges by
     * type for variety.
     */
    public static int calculateCR(Size size, Type type) {
        return ((2 * size.ordinal() + type.ordinal()) % 27) - 3;
    }
}
