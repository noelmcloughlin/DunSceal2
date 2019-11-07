/*
 * Copyright (C) 2017 The Android Open Source Project
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

package org.noel.dunsceal.data

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import org.noel.dunsceal.data.source.DefaultDunsRepository
import org.noel.dunsceal.data.source.DunsDataSource
import org.noel.dunsceal.data.source.DunsRepository
import org.noel.dunsceal.data.source.local.relationalDB.DunsLocalDataSource
import org.noel.dunsceal.data.source.local.relationalDB.DunDatabase
import org.noel.dunsceal.data.source.remote.DunsRemoteDataSource
import kotlinx.coroutines.runBlocking

/**
 * A Service Locator for the [DunsRepository]. This is the prod version, with a
 * the "real" [DunsRemoteDataSource].
 */
object ServiceLocator {

    private val lock = Any()
    private var database: DunDatabase? = null
    @Volatile
    var dunsRepository: DunsRepository? = null
        @VisibleForTesting set

    fun provideDunsRepository(context: Context): DunsRepository {
        synchronized(this) {
            return dunsRepository
                ?: dunsRepository
                ?: createDunsRepository(context)
        }
    }

    private fun createDunsRepository(context: Context): DunsRepository {
        return DefaultDunsRepository(DunsRemoteDataSource, createDunLocalDataSource(context))
    }

    private fun createDunLocalDataSource(context: Context): DunsDataSource {
        val database = database
            ?: createDataBase(context)
        return DunsLocalDataSource(database.dunDao())
    }

    private fun createDataBase(context: Context): DunDatabase {
        val result = Room.databaseBuilder(
            context.applicationContext,
            DunDatabase::class.java, "Duns.db"
        ).build()
        database = result
        return result
    }

    @VisibleForTesting
    fun resetRepository() {
        synchronized(lock) {
            runBlocking {
                DunsRemoteDataSource.deleteAllDuns()
            }
            // Clear all data to avoid test pollution.
            database?.apply {
                clearAllTables()
                close()
            }
            database = null
            dunsRepository = null
        }
    }
}
