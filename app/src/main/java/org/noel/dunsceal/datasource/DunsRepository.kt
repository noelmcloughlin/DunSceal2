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

package org.noel.dunsceal.datasource

import org.noel.dunsceal.util.Result
import org.noel.dunsceal.model.Dun

/**
 * Interface to the data layer.
 */
interface DunsRepository {

    suspend fun getDuns(): Result<List<Dun>>

    suspend fun getDuns(forceUpdate: Boolean = false): Result<List<Dun>>

    suspend fun getDun(taskId: String, forceUpdate: Boolean = false): Result<Dun>

    suspend fun saveDun(dun: Dun)

    suspend fun updateDun(dun: Dun)

    suspend fun completeDun(dun: Dun)

    suspend fun completeDun(dunId: String)

    suspend fun activateDun(dun: Dun)

    suspend fun activateDun(dunId: String)

    suspend fun clearCompletedDuns()

    suspend fun deleteAllDuns()

    suspend fun deleteDun(dunId: String)
}
