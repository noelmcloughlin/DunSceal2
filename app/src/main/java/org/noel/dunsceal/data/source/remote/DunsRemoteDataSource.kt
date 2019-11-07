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
package org.noel.dunsceal.data.source.remote

import org.noel.dunsceal.data.Result
import org.noel.dunsceal.data.Result.Error
import org.noel.dunsceal.data.Result.Success
import org.noel.dunsceal.data.Dun
import org.noel.dunsceal.data.source.DunsDataSource
import kotlinx.coroutines.delay

/**
 * Implementation of the data source that adds a latency simulating network.
 */
object DunsRemoteDataSource : DunsDataSource {

    private const val SERVICE_LATENCY_IN_MILLIS = 2000L

    private var DUNS_SERVICE_DATA = LinkedHashMap<String, Dun>(2)

    init {
      addDun("Build tower in Pisa", "Ground looks good, no foundation work required.")
      addDun("Finish bridge in Tacoma", "Found awesome girders at half the cost!")
    }

    override suspend fun getDuns(): Result<List<Dun>> {
        // Simulate network by delaying the execution.
        val duns = DUNS_SERVICE_DATA.values.toList()
        delay(SERVICE_LATENCY_IN_MILLIS)
        return Result.Success(duns)
    }

    override suspend fun getDun(dunId: String): Result<Dun> {
        // Simulate network by delaying the execution.
        delay(SERVICE_LATENCY_IN_MILLIS)
        DUNS_SERVICE_DATA[dunId]?.let {
            return Success(it)
        }
        return Error(Exception("Dun not found"))
    }

    private fun addDun(title: String, description: String) {
        val newDun = Dun(title, description)
        DUNS_SERVICE_DATA[newDun.id] = newDun
    }

    override suspend fun saveDun(dun: Dun) {
        DUNS_SERVICE_DATA[dun.id] = dun
    }

    override suspend fun completeDun(dun: Dun) {
        val completedDun = Dun(dun.title, dun.description, true, dun.id)
        DUNS_SERVICE_DATA[dun.id] = completedDun
    }

    override suspend fun completeDun(dunId: String) {
        // Not required for the remote data source
    }

    override suspend fun activateDun(dun: Dun) {
        val plannedDun = Dun(dun.title, dun.description, false, dun.id)
        DUNS_SERVICE_DATA[dun.id] = plannedDun
    }

    override suspend fun activateDun(dunId: String) {
        // Not required for the remote data source
    }

    override suspend fun clearCompletedDuns() {
        DUNS_SERVICE_DATA = DUNS_SERVICE_DATA.filterValues {
            !it.isCompleted
        } as LinkedHashMap<String, Dun>
    }

    override suspend fun deleteAllDuns() {
        DUNS_SERVICE_DATA.clear()
    }

    override suspend fun deleteDun(dunId: String) {
        DUNS_SERVICE_DATA.remove(dunId)
    }
}
