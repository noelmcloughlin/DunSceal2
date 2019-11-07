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

package org.noel.dunsceal.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.noel.dunsceal.data.Result.Success
import org.noel.dunsceal.data.Dun
import org.noel.dunsceal.data.source.DunsRepository
import org.noel.dunsceal.util.wrapEspressoIdlingResource
import kotlinx.coroutines.launch
import org.noel.dunsceal.util.getPlannedAndCompletedStats

/**
 * ViewModel for the statistics screen.
 */
class StatisticsViewModel(
    private val dunsRepository: DunsRepository
) : ViewModel() {

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _error = MutableLiveData<Boolean>()
    val error: LiveData<Boolean> = _error

    /**
     * Controls whether the stats are shown or a "No data" message.
     */
    private val _empty = MutableLiveData<Boolean>()
    val empty: LiveData<Boolean> = _empty

    private val _plannedDunsPercent = MutableLiveData<Float>()
    val plannedDunsPercent: LiveData<Float> = _plannedDunsPercent

    private val _completedDunsPercent = MutableLiveData<Float>()
    val completedDunsPercent: LiveData<Float> = _completedDunsPercent

    private var plannedDuns = 0

    private var completedDuns = 0

    init {
        start()
    }

    fun start() {
        if (_dataLoading.value == true) {
            return
        }
        _dataLoading.value = true

      wrapEspressoIdlingResource {
        viewModelScope.launch {
          dunsRepository.getDuns().let { result ->
            if (result is Success) {
              _error.value = false
              computeStats(result.data)
            } else {
              _error.value = true
              plannedDuns = 0
              completedDuns = 0
              computeStats(null)
            }
          }
        }
      }
    }

    fun refresh() {
        start()
    }

    /**
     * Called when new data is ready.
     */
    private fun computeStats(duns: List<Dun>?) {
        getPlannedAndCompletedStats(duns).let {
            _plannedDunsPercent.value = it.plannedDunsPercent
            _completedDunsPercent.value = it.completedDunsPercent
        }
        _empty.value = duns.isNullOrEmpty()
        _dataLoading.value = false
    }
}
