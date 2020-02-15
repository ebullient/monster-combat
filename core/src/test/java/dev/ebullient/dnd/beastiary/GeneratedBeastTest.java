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

import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class GeneratedBeastTest {

    @Test
    public void testCalculateAC() {
        // Fixd formula, but this gives an idea of what is returned
        Assert.assertEquals(10, GeneratedBeast.calculateAC(8, -3));
        Assert.assertEquals(11, GeneratedBeast.calculateAC(10, -3));
        Assert.assertEquals(15, GeneratedBeast.calculateAC(10, 8));
        Assert.assertEquals(18, GeneratedBeast.calculateAC(8, 20));
    }

}