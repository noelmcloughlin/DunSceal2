package org.noel.dunsceal.datasource.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.jetbrains.anko.AnkoLogger
import org.noel.dunsceal.model.Dun
import org.noel.dunsceal.util.Result
import org.noel.dunsceal.helpers.*
import java.util.*
import org.noel.dunsceal.datasource.DunsRepository

val JSON_FILE = "duns.json"
val gsonBuilder = GsonBuilder().setPrettyPrinting().create()
val listType = object : TypeToken<ArrayList<Dun>>() {}.type

fun generateRandomId(): String {
  return UUID.randomUUID().toString()
}

open class JsonDatabase : DunsRepository, AnkoLogger {

  val context: Context
  var duns = mutableListOf<Dun>()

  constructor (context: Context) {
    this.context = context
    if (exists(context, JSON_FILE)) {
      deserialize()
    }
  }

  private fun serialize() {
    val jsonString = gsonBuilder.toJson(duns, listType)
    write(context, JSON_FILE, jsonString)
  }

  private fun deserialize() {
    val jsonString = read(context, JSON_FILE)
    duns = Gson().fromJson(jsonString, listType)
  }

  /** Methods to be implemented here.
   *
   * getDuns(forceUpdate: Boolean = false): Result<List<Dun>>
   * getDun(dunId: String, forceUpdate: Boolean = false): Result<Dun>
   * saveDun(dun: Dun)
   * updateDun(dun: Dun)
   * completeDun(dun: Dun)
   * completeDun(dunId: String)
   * activateDun(dun: Dun)
   * activateDun(dunId: String)
   * clearCompletedDuns()
   * deleteAllDuns()
   * deleteDun(dunId: String)
   **/

  override suspend fun getDuns(forceUpdate: Boolean): Result<List<Dun>> {
    return Result.Success(duns)
  }

  override suspend fun getDuns(): Result<List<Dun>> {
    return Result.Success(duns)
  }

  private suspend fun getDun(dunId: String): Result<Dun> {
    val dunsList = getDuns() as ArrayList<Dun>
    val foundDun: Dun? = dunsList.find { p -> p.id == dunId }
    return Result.Success(foundDun!!)
  }

  override suspend fun getDun(dunId: String, forceUpdate: Boolean): Result<Dun> {
    return getDun(dunId)
  }

  override suspend fun saveDun(dun: Dun) {
    dun.id = generateRandomId()
    duns.add(dun)
    serialize()
  }

  override suspend fun updateDun(dun: Dun) {
    val dunsList = getDuns() as ArrayList<Dun>
    var foundDun: Dun? = dunsList.find { p -> p.id == dun.id }
    if (foundDun != null) {
      foundDun.description = dun.description
      foundDun.isCompleted = dun.isCompleted
      foundDun.image = dun.image
      foundDun.lat = dun.lat
      foundDun.lng = dun.lng
      foundDun.zoom = dun.zoom
    }
    serialize()
  }

  override suspend fun completeDun(dun: Dun) {
    dun.isCompleted = true
    updateDun(dun)
  }

  override suspend fun completeDun(dunId: String) {
    val dunsList = getDuns() as ArrayList<Dun>
    var foundDun: Dun? = dunsList.find { p -> p.id == dunId }
    if (foundDun != null) {
      foundDun.isCompleted = true
      completeDun(foundDun)
    }
  }

  //    @Query("UPDATE tasks SET completed = :completed WHERE entryid = :taskId")
  override suspend fun activateDun(dun: Dun) {

    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override suspend fun activateDun(dunId: String) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override suspend fun clearCompletedDuns() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override suspend fun deleteAllDuns() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override suspend fun deleteDun(dunId: String) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}