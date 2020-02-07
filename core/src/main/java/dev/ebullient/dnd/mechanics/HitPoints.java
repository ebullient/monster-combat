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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The number of hit points that beasts start with is described by a string that
 * looks either like "27" or "285 (30d8 + 150)".
 *
 * In the latter case, we can either take the average, or do the rolls + math to
 * get a dynamic value.
 */
public class HitPoints {
    public static final Pattern HP = Pattern.compile("(\\d+) ?(?:\\(([-+ d0-9]*)\\))?");

    public static String validate(String text) {
        Matcher hp = HP.matcher(text.trim());
        if (hp.matches()) {
            return text.replace(" ", ""); // compress
        } else {
            throw new IllegalArgumentException("Invalid string: " + text);
        }
    }

    public static int startingHitPoints(String monsterName, String hpDescription, Dice.Method method) {
        if (hpDescription != null) {
            Matcher m = HP.matcher(hpDescription);
            if (m.matches()) {
                if (method == Dice.Method.USE_AVERAGE || m.group(2) == null) {
                    return Integer.parseInt(m.group(1));
                }
                return Dice.roll(m.group(2));
            }
        }
        throw new IllegalArgumentException(monsterName + " has a bad hitpoints string: " + hpDescription);
    }

    /**
     * https://theangrygm.com/monster-building-201-the-dd-monster-dissection-lab/
     */
    public static int startingHitPoints(int constitution, int cr, Size size) {
        // How many times to roll for hit points? .. that's based on target CR
        int n = (cr <= 7) ? 1 : (cr / 3);

        // Add constitution modifier for each dice roll
        int modifier = n * constitution;
        int hp = 0;

        // For math, we're going to use the average value of the roll
        switch (size) {
            case TINY:
                hp = (int) ((Dice.averageRoll(4) * n) + modifier);
                break;
            case SMALL:
                hp = (int) ((Dice.averageRoll(6) * n) + modifier);
                break;
            default:
            case MEDIUM:
                hp = (int) ((Dice.averageRoll(8) * n) + modifier);
                break;
            case LARGE:
                hp = (int) ((Dice.averageRoll(10) * n) + modifier);
                break;
            case HUGE:
                hp = (int) ((Dice.averageRoll(12) * n) + modifier);
                break;
            case GARGANTUAN:
                hp = (int) ((Dice.averageRoll(20) * n) + modifier);
                break;
        }

        // System.out.println("cr="+cr+ ", n="+n+", con="+constitution +", size="+size+", hp="+hp);
        return hp;
    }
}
