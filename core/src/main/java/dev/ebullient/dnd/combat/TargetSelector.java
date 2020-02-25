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

/**
 * Wrap package-internal target selector
 */
public interface TargetSelector {

    public static final TargetSelector SelectByHighestRelativeHealth = EncounterTargetSelector.SelectByHighestRelativeHealth;
    public static final TargetSelector SelectByLowestRelativeHealth = EncounterTargetSelector.SelectByLowestRelativeHealth;

    public static final TargetSelector SelectByHighestChallengeRating = EncounterTargetSelector.SelectByHighestChallengeRating;
    public static final TargetSelector SelectByLowestChallengeRating = EncounterTargetSelector.SelectByLowestChallengeRating;

    public static final TargetSelector SelectBiggest = EncounterTargetSelector.SelectBiggest;
    public static final TargetSelector SelectSmallest = EncounterTargetSelector.SelectSmallest;

    public static final TargetSelector SelectAtRandom = EncounterTargetSelector.SelectAtRandom;

}
