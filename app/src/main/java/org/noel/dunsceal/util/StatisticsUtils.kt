/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.noel.dunsceal.util

import org.noel.dunsceal.model.Dun

/**
 * Function that does some trivial computation. Used to showcase unit tests.
 */
internal fun getPlannedAndCompletedStats(duns: List<Dun>?): StatsResult {

    return if (duns == null || duns.isEmpty()) {
      StatsResult(0f, 0f)
    } else {
        val totalDuns = duns.size
        val numberOfplannedDuns = duns.count { it.isPlanned }
      StatsResult(
          plannedDunsPercent = 100f * numberOfplannedDuns / duns.size,
          completedDunsPercent = 100f * (totalDuns - numberOfplannedDuns) / duns.size
      )
    }
}

data class StatsResult(val plannedDunsPercent: Float, val completedDunsPercent: Float)
