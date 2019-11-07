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
package org.noel.dunsceal.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Immutable model class for a Dun. In order to compile with Room, we can't use @JvmOverloads to
 * generate multiple constructors.
 *
 * @param title       title of the dun
 * @param description description of the dun
 * @param isCompleted whether or not this dun is completed
 * @param id          id of the dun
 */
@Entity(tableName = "duns")
data class Dun @JvmOverloads constructor(
    @ColumnInfo(name = "title") var title: String = "",
    @ColumnInfo(name = "description") var description: String = "",
    @ColumnInfo(name = "completed") var isCompleted: Boolean = false,
    @PrimaryKey @ColumnInfo(name = "entryid") var id: String = UUID.randomUUID().toString()
) {

    val titleForList: String
        get() = if (title.isNotEmpty()) title else description


    val isPlanned
        get() = !isCompleted

    val isEmpty
        get() = title.isEmpty() || description.isEmpty()
}
