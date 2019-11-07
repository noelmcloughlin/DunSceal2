package org.noel.dunsceal.data.source.local

import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.noel.dunsceal.model.DunModel
import org.noel.dunsceal.model.DunStore

var lastId = 0L

internal fun getId(): Long {
    return lastId++
}

class DunMemStore : DunStore, AnkoLogger {

    val duns = ArrayList<DunModel>()

    override fun findAll(): ArrayList<DunModel> {
        return duns
    }

    override fun create(dun: DunModel) {
        dun.id = getId()
        duns.add(dun)
        logAll()
    }

    override fun update(dun: DunModel) {
        var foundDun: DunModel? = duns.find { p -> p.id == dun.id }
        if (foundDun != null) {
            foundDun.name = dun.name
            foundDun.description = dun.description
            foundDun.visited = dun.visited
            foundDun.image = dun.image
            logAll();
        }
    }

    fun logAll() {
        duns.forEach { info("${it}") }
    }
}