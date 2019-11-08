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
import org.noel.dunsceal.util.Result.Error
import org.noel.dunsceal.util.Result.Success
import org.noel.dunsceal.model.Dun
import org.noel.dunsceal.util.EspressoIdlingResource
import org.noel.dunsceal.util.wrapEspressoIdlingResource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Concrete implementation to load duns from the data sources into a cache.
 *
 * To simplify the sample, I only use a local data source.
 */
class DefaultDunsRepository(
    private val dunsLocalDataSource: DunsDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DunsRepository {

  private var cachedDuns: ConcurrentMap<String, Dun>? = null

  override suspend fun getDuns(): Result<MutableList<Dun>> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override suspend fun getDuns(forceUpdate: Boolean): Result<List<Dun>> {

    wrapEspressoIdlingResource {
      return withContext(ioDispatcher) {
        // Respond immediately with cache if available and not dirty
        if (!forceUpdate) {
          cachedDuns?.let { cachedDuns ->
            return@withContext Success(cachedDuns.values.sortedBy { it.id })
          }
        }
        val newDuns = fetchDunsFromLocal(forceUpdate)
        // Refresh the cache with the new duns
        (newDuns as? Success)?.let { refreshCache(it.data) }

        cachedDuns?.values?.let { duns ->
          return@withContext Success(duns.sortedBy { it.id })
        }
        (newDuns as? Success)?.let {
          if (it.data.isEmpty()) {
            return@withContext Success(it.data)
          }
        }
        return@withContext Error(Exception("Illegal state"))
      }
    }
  }

  private suspend fun fetchDunsFromLocal(forceUpdate: Boolean): Result<List<Dun>> {
    // Don't read from local if it's forced
    if (forceUpdate) {
      return Error(Exception("Can't force refresh: remote data source is unavailable"))
    }

    // Local if remote fails
    val localDuns = dunsLocalDataSource.getDuns()
    if (localDuns is Success) return localDuns
    return Error(Exception("Error fetching from remote and local"))
  }

  /**
   * Relies on [getDuns] to fetch data and picks the dun with the same ID.
   */
  override suspend fun getDun(dunId: String, forceUpdate: Boolean): Result<Dun> {

    wrapEspressoIdlingResource {

      return withContext(ioDispatcher) {
        // Respond immediately with cache if available
        if (!forceUpdate) {
          getDunWithId(dunId)?.let {
            EspressoIdlingResource.decrement() // Set app as idle.
            return@withContext Success(it)
          }
        }

        val newDun = fetchDunFromLocal(dunId, forceUpdate)

        // Refresh the cache with the new duns
        (newDun as? Success)?.let { cacheDun(it.data) }
        return@withContext newDun
      }
    }
  }

  private suspend fun fetchDunFromLocal(
      dunId: String,
      forceUpdate: Boolean
  ): Result<Dun> {
    // Don't read from local if it's forced
    if (forceUpdate) {
      return Error(Exception("Refresh failed"))
    }

    // Local if remote fails
    val localDuns = dunsLocalDataSource.getDun(dunId)
    if (localDuns is Success) return localDuns
    return Error(Exception("Error fetching from remote and local"))
  }

  override suspend fun saveDun(dun: Dun) {
    // Do in memory cache update to keep the app UI up to date
    cacheAndPerform(dun) {
      coroutineScope {
        launch { dunsLocalDataSource.saveDun(it) }
      }
    }
  }

  override suspend fun updateDun(dun: Dun) {
    // Do in memory cache update to keep the app UI up to date
    cacheAndPerform(dun) {
      it.isCompleted = true
      coroutineScope {
        launch { dunsLocalDataSource.updateDun(it) }
      }
    }
  }

  suspend fun updateDun(dunId: String) {
    withContext(ioDispatcher) {
      getDunWithId(dunId)?.let {
        updateDun(it)
      }
    }
  }

  override suspend fun completeDun(dun: Dun) {
    // Do in memory cache update to keep the app UI up to date
    cacheAndPerform(dun) {
      it.isCompleted = true
      coroutineScope {
        launch { dunsLocalDataSource.completeDun(it) }
      }
    }
  }

  override suspend fun completeDun(dunId: String) {
    withContext(ioDispatcher) {
      getDunWithId(dunId)?.let {
        completeDun(it)
      }
    }
  }

  override suspend fun activateDun(dun: Dun) = withContext(ioDispatcher) {
    // Do in memory cache update to keep the app UI up to date
    cacheAndPerform(dun) {
      it.isCompleted = false
      coroutineScope {
        launch { dunsLocalDataSource.activateDun(it) }
      }
    }
  }

  override suspend fun activateDun(dunId: String) {
    withContext(ioDispatcher) {
      getDunWithId(dunId)?.let {
        activateDun(it)
      }
    }
  }

  override suspend fun clearCompletedDuns() {
    coroutineScope {
      launch { dunsLocalDataSource.clearCompletedDuns() }
    }
    withContext(ioDispatcher) {
      cachedDuns?.entries?.removeAll { it.value.isCompleted }
    }
  }

  override suspend fun deleteAllDuns() {
    withContext(ioDispatcher) {
      coroutineScope {
        launch { dunsLocalDataSource.deleteAllDuns() }
      }
    }
    cachedDuns?.clear()
  }

  override suspend fun deleteDun(dunId: String) {
    coroutineScope {
      launch { dunsLocalDataSource.deleteDun(dunId) }
    }
    cachedDuns?.remove(dunId)
  }

  private fun refreshCache(duns: List<Dun>) {
    cachedDuns?.clear()
    duns.sortedBy { it.id }.forEach {
      cacheAndPerform(it) {}
    }
  }

  private fun getDunWithId(id: String) = cachedDuns?.get(id)

  private fun cacheDun(dun: Dun): Dun {
    val cachedDun = Dun(dun.title, dun.description, dun.isCompleted, dun.id)
    // Create if it doesn't exist.
    if (cachedDuns == null) {
      cachedDuns = ConcurrentHashMap()
    }
    cachedDuns?.put(cachedDun.id, cachedDun)
    return cachedDun
  }

  private inline fun cacheAndPerform(dun: Dun, perform: (Dun) -> Unit) {
    val cachedDun = cacheDun(dun)
    perform(cachedDun)
  }
}