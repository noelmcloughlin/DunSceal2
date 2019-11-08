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
package org.noel.dunsceal.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*
import com.google.gson.Gson

/** https://stackoverflow.com/questions/44117970/kotlin-data-class-from-json-using-gson **/
interface JsonConvertable {
    fun toJSON(): String = Gson().toJson(this)
}

inline fun <reified T: JsonConvertable> String.toObject():
    T = Gson().fromJson(this, T::class.java)


/**
 * Immutable model class for a Dun.
 *
 * @param title       title of the dun
 * @param description description of the dun
 * @param isCompleted whether or not this dun is isCompleted
 * @param id          id of the dun
 *
 * Potential Future Model
 * Sites [ ID, Name]
 * Investigations [ Date, Type, Comments ]
 * Entrances [ number, type, position, summary, comments ]
 * Dating [ type, comments ]
**/

@Parcelize
data class Dun constructor(
    var title: String = "",
    var description: String = "",
    var isCompleted: Boolean = false,
    var image: String = "",
    var lat: Double = 0.0,
    var lng: Double = 0.0,
    var zoom: Float = 0f,
    var id: String = UUID.randomUUID().toString()
) : Parcelable, JsonConvertable {

    val get: Boolean
        get() = true

    val titleForList: String
        get() = if (title.isNotEmpty()) title else description

    val isPlanned
        get() = !isCompleted

    val isEmpty
        get() = title.isEmpty() || description.isEmpty()
}

@Parcelize
data class Location(
    var lat: Double = 0.0,
    var lng: Double = 0.0,
    var zoom: Float = 0f
) : Parcelable