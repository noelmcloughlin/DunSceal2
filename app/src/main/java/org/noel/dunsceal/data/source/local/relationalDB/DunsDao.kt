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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.noel.dunsceal.data.Dun

/**
 * Data Access Object for the duns table.
 */
@Dao
interface DunsDao {

    /**
     * Select all duns from the duns table.
     *
     * @return all duns.
     */
    @Query("SELECT * FROM Duns")
    suspend fun getDuns(): List<Dun>

    /**
     * Select a dun by id.
     *
     * @param dunId the dun id.
     * @return the dun with dunId.
     */
    @Query("SELECT * FROM Duns WHERE entryid = :dunId")
    suspend fun getDunById(dunId: String): Dun?

    /**
     * Insert a dun in the database. If the dun already exists, replace it.
     *
     * @param dun the dun to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDun(dun: Dun)

    /**
     * Update a dun.
     *
     * @param dun dun to be updated
     * @return the number of duns updated. This should always be 1.
     */
    @Update
    suspend fun updateDun(dun: Dun): Int

    /**
     * Update the complete status of a dun
     *
     * @param dunId    id of the dun
     * @param completed status to be updated
     */
    @Query("UPDATE Duns SET completed = :completed WHERE entryid = :dunId")
    suspend fun updateCompleted(dunId: String, completed: Boolean)

    /**
     * Delete a dun by id.
     *
     * @return the number of duns deleted. This should always be 1.
     */
    @Query("DELETE FROM Duns WHERE entryid = :dunId")
    suspend fun deleteDunById(dunId: String): Int

    /**
     * Delete all duns.
     */
    @Query("DELETE FROM Duns")
    suspend fun deleteDuns()

    /**
     * Delete all completed duns from the table.
     *
     * @return the number of duns deleted.
     */
    @Query("DELETE FROM Duns WHERE completed = 1")
    suspend fun deleteCompletedDuns(): Int
}
