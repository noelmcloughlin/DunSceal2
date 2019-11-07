package org.noel.dunsceal.data.source.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.jetbrains.anko.AnkoLogger
import org.noel.dunsceal.helpers.*
import org.noel.dunsceal.model.DunModel
import org.noel.dunsceal.model.DunStore
import java.util.*

val JSON_FILE = "duns.json"
val gsonBuilder = GsonBuilder().setPrettyPrinting().create()
val listType = object : TypeToken<ArrayList<DunModel>>() {}.type

fun generateRandomId(): Long {
    return Random().nextLong()
}

class DunJsonStore : DunStore, AnkoLogger {

    val context: Context
    var duns = mutableListOf<DunModel>()

    constructor (context: Context) {
        this.context = context
        if (exists(context, JSON_FILE)) {
            deserialize()
        }
    }

    override fun findAll(): MutableList<DunModel> {
        return duns
    }

    override fun create(dun: DunModel) {
        dun.id = generateRandomId()
        duns.add(dun)
        serialize()
    }


    override fun update(dun: DunModel) {
        // todo
    }

    /**override fun delete(dunsceal: DunModel) {
        // todo
    } */

    private fun serialize() {
        val jsonString = gsonBuilder.toJson(duns, listType)
        write(context, JSON_FILE, jsonString)
    }

    private fun deserialize() {
        val jsonString = read(context, JSON_FILE)
        duns = Gson().fromJson(jsonString, listType)
    }
}