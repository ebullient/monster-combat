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
    static final Pattern HP = Pattern.compile("(\\d+)\\s*(\\((.*)\\))?");

    public static String validate(String text) {
        Matcher hp = HP.matcher(text);
        if (hp.matches()) {
            return text.replace(" ", ""); // compress
        } else {
            throw new IllegalArgumentException("Invalid string: " + text);
        }
    }
}
