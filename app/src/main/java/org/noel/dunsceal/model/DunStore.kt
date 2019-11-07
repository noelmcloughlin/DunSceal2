package org.noel.dunsceal.model

import org.noel.dunsceal.model.DunModel

interface DunStore {
    fun findAll(): List<DunModel>
    fun create(dun: DunModel)
    fun update(dun: DunModel)
    //fun delete(dun: DunModel)
}