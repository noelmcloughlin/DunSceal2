package org.noel.dunsceal.datasource.local

import org.noel.dunsceal.model.Dun

/**
 * This interface describes the kinds of Data Access Operations available.
 *
 * Since we plan on using different formats for reading and writing data,
 * we should use an interface as an abstraction point.
 * https://stonesoupprogramming.com/2017/11/01/kotlin-data-access-object
 */
interface DunsDao {

  /**
   * Select all duns from the duns table.
   *
   * @return all duns.
   */
  suspend fun getDuns(): List<Dun>

  /**
   * Select a dun by id.
   *
   * @param dunId the dun id.
   * @return the dun with dunId.
   */
  suspend fun getDunById(dunId: String): Dun?

  /**
   * Insert a dun in the database. If the dun already exists, replace it.
   *
   * @param dun the dun to be inserted.
   */
  suspend fun insertDun(dun: Dun)

  /**
   * Update a dun.
   *
   * @param dun dun to be updated
   * @return the number of duns updated. This should always be 1.
   */
  suspend fun updateDun(dun: Dun)
  
  /**
   * Update the complete status of a dun
   *
   * @param dunId    id of the dun
   * @param completed status to be updated
   */
  suspend fun updateCompleted(dunId: String, completed: Boolean)

  /**
   * Delete a dun by id.
   *
   * @return the number of duns deleted. This should always be 1.
   */
  suspend fun deleteDunById(dunId: String): Int

  /**
   * Delete all duns.
   */
  suspend fun deleteDuns()

  /**
   * Delete all completed duns from the table.
   *
   * @return the number of duns deleted.
   */
  suspend fun deleteCompletedDuns(): Int
}