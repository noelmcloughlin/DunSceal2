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
package org.noel.dunsceal.data.source.local.relationalDB

import org.noel.dunsceal.data.Result
import org.noel.dunsceal.data.Result.Error
import org.noel.dunsceal.data.Result.Success
import org.noel.dunsceal.data.Dun
import org.noel.dunsceal.data.source.DunsDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.noel.dunsceal.data.source.local.relationalDB.DunsDao

/**
 * Concrete implementation of a data source as a db.
 */
class DunsLocalDataSource internal constructor(
    private val dunsDao: DunsDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DunsDataSource {

    override suspend fun getDuns(): Result<List<Dun>> = withContext(ioDispatcher) {
        return@withContext try {
            Success(dunsDao.getDuns())
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun getDun(dunId: String): Result<Dun> = withContext(ioDispatcher) {
        try {
            val dun = dunsDao.getDunById(dunId)
            if (dun != null) {
                return@withContext Success(dun)
            } else {
                return@withContext Error(Exception("Dun not found!"))
            }
        } catch (e: Exception) {
            return@withContext Error(e)
        }
    }

    override suspend fun saveDun(dun: Dun) = withContext(ioDispatcher) {
        dunsDao.insertDun(dun)
    }

    override suspend fun completeDun(dun: Dun) = withContext(ioDispatcher) {
        dunsDao.updateCompleted(dun.id, true)
    }

    override suspend fun completeDun(dunId: String) {
        dunsDao.updateCompleted(dunId, true)
    }

    override suspend fun activateDun(dun: Dun) = withContext(ioDispatcher) {
        dunsDao.updateCompleted(dun.id, false)
    }

    override suspend fun activateDun(dunId: String) {
        dunsDao.updateCompleted(dunId, false)
    }

    override suspend fun clearCompletedDuns() = withContext<Unit>(ioDispatcher) {
        dunsDao.deleteCompletedDuns()
    }

    override suspend fun deleteAllDuns() = withContext(ioDispatcher) {
        dunsDao.deleteDuns()
    }

    override suspend fun deleteDun(dunId: String) = withContext<Unit>(ioDispatcher) {
        dunsDao.deleteDunById(dunId)
    }
}
