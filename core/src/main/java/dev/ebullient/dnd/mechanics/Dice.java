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

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dice {
    public static final Pattern AVG_ROLL_MOD = Pattern.compile("(\\d+)(?:\\(([-+d0-9]+)\\))?");

    public enum Method {
        USE_AVERAGE,
        ROLL
    }

    public enum Constraint {
        ADVANTAGE,
        DISADVANTAGE,
        FAIL,
        NONE
    }

    static final Random random = new Random();

    /** @return a value in a range including 0: [0, bound) */
    public static final int range(int bound) {
        return random.nextInt(bound);
    }

    /** @return the value of a custom die roll: [1, bound] */
    public static final int customDie(int bound) {
        return random.nextInt(bound) + 1;
    }

    /**
     * @return average roll per sides
     */
    public static final double averageRoll(int sides) {
        switch (sides) {
            case 4:
                return 2.5;
            case 6:
                return 3.5;
            case 8:
                return 4.5;
            case 10:
                return 5.5;
            case 12:
                return 6.5;
            case 20:
                return 10.5;
            default:
                return (sides / 2) + .5;
        }
    }

    /** @return the value of a 1d4 roll: [1,4] */
    public static final int d4() {
        return random.nextInt(4) + 1;
    }

    /** @return the value of n 1d4 rolls */
    public static final int d4(int n) {
        int total = 0;
        for (int i = 0; i < n; i++) {
            total += d4();
        }
        return total;
    }

    /** @return the value of a 1d6 roll: [1,6] */
    public static final int d6() {
        return random.nextInt(6) + 1;
    }

    /** @return the value of n 1d6 rolls */
    public static final int d6(int n) {
        int total = 0;
        for (int i = 0; i < n; i++) {
            total += d6();
        }
        return total;
    }

    /** @return the value of a 1d8 roll: [1,8] */
    public static final int d8() {
        return random.nextInt(8) + 1;
    }

    /** @return the value of n 1d8 rolls */
    public static final int d8(int n) {
        int total = 0;
        for (int i = 0; i < n; i++) {
            total += d8();
        }
        return total;
    }

    /** @return the value of a 1d10 roll: [1,10] */
    public static final int d10() {
        return random.nextInt(10) + 1;
    }

    /** @return the value of n 1d10 rolls */
    public static final int d10(int n) {
        int total = 0;
        for (int i = 0; i < n; i++) {
            total += d10();
        }
        return total;
    }

    /** @return the value of a 1d12 roll: [1,12] */
    public static final int d12() {
        return random.nextInt(12) + 1;
    }

    /** @return the value of n 1d12 rolls */
    public static final int d12(int n) {
        int total = 0;
        for (int i = 0; i < n; i++) {
            total += d12();
        }
        return total;
    }

    /** @return the value of a 1d20 roll: [1,20] */
    public static final int d20() {
        return random.nextInt(20) + 1;
    }

    public static final int d20(Constraint c) {
        int roll1 = Dice.d20();
        int roll2 = Dice.d20();

        if (c == Constraint.DISADVANTAGE) {
            return Math.min(roll1, roll2);
        } else if (c == Constraint.ADVANTAGE) {
            return Math.max(roll1, roll2);
        } else if (c == Constraint.FAIL) {
            return 1;
        }
        return roll1;
    }

    /** @return the value of n 1d20 rolls */
    public static final int d20(int n) {
        int total = 0;
        for (int i = 0; i < n; i++) {
            total += d20();
        }
        return total;
    }

    public static int roll(String pattern, Method method) {
        if (pattern != null) {
            Matcher m = AVG_ROLL_MOD.matcher(pattern);
            if (m.matches()) {
                if (method == Method.USE_AVERAGE || m.group(2) == null) {
                    return Integer.parseInt(m.group(1));
                }
                return Dice.roll(m.group(2));
            }
        }
        throw new IllegalArgumentException("Bad roll pattern: " + pattern);
    }

    /** @return the result of a specified roll: 1d6+2 or 5d10+9+1d10 or 1d6-1 */
    public static int roll(String pattern) {
        final Pattern dice = Pattern.compile("(\\d+)d(\\d+)"); // x rolls of x-sided die
        final Pattern add = Pattern.compile("(\\d+)\\s*(\\+|-)\\s*(\\d+)");

        Matcher rolls = dice.matcher(pattern);
        StringBuffer str = new StringBuffer();

        while (rolls.find()) {
            int n = Integer.parseInt(rolls.group(1));
            int result = 0;
            switch (rolls.group(2)) {
                case "4":
                    result = d4(n);
                    break;
                case "6":
                    result = d6(n);
                    break;
                case "8":
                    result = d8(n);
                    break;
                case "10":
                    result = d10(n);
                    break;
                case "20":
                    result = d20(n);
                    break;
            }
            rolls.appendReplacement(str, Integer.toString(result));
        }
        rolls.appendTail(str);

        pattern = str.toString();

        // We're down to simple math now: 1+20-12
        Matcher sum = add.matcher(pattern);
        while (sum.find()) {
            int x = Integer.parseInt(sum.group(1));
            int y = Integer.parseInt(sum.group(3));
            switch (sum.group(2)) {
                case "+":
                    pattern = sum.replaceFirst(Integer.toString(x + y));
                    break;
                case "-":
                    pattern = sum.replaceFirst(Integer.toString(x - y));
                    break;
            }
            sum.reset(pattern);
        }

        try {
            return Integer.parseInt(pattern);
        } catch (NumberFormatException nfe) {
            System.err.println("Error parsing " + pattern + ": " + nfe.getMessage());
            throw nfe;
        }
    }
}
