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

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.noel.dunsceal.util.Event
import org.noel.dunsceal.R
import org.noel.dunsceal.data.Result
import org.noel.dunsceal.data.Result.Success
import org.noel.dunsceal.data.Dun
import org.noel.dunsceal.data.source.DunsRepository
import org.noel.dunsceal.util.wrapEspressoIdlingResource
import kotlinx.coroutines.launch

/**
 * ViewModel for the Details screen.
 */
class DunDetailViewModel(
    private val dunsRepository: DunsRepository
) : ViewModel() {

    private val _dun = MutableLiveData<Dun>()
    val dun: LiveData<Dun> = _dun

    private val _isDataAvailable = MutableLiveData<Boolean>()
    val isDataAvailable: LiveData<Boolean> = _isDataAvailable

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _editDunEvent = MutableLiveData<Event<Unit>>()
    val editDunEvent: LiveData<Event<Unit>> = _editDunEvent

    private val _deleteDunEvent = MutableLiveData<Event<Unit>>()
    val deleteDunEvent: LiveData<Event<Unit>> = _deleteDunEvent

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    private val dunId: String?
        get() = _dun.value?.id

    // This LiveData depends on another so we can use a transformation.
    val completed: LiveData<Boolean> = Transformations.map(_dun) { input: Dun? ->
        input?.isCompleted ?: false
    }


    fun deleteDun() = viewModelScope.launch {
        dunId?.let {
            dunsRepository.deleteDun(it)
            _deleteDunEvent.value = Event(Unit)
        }
    }

    fun editDun() {
        _editDunEvent.value = Event(Unit)
    }

    fun setCompleted(completed: Boolean) = viewModelScope.launch {
        val dun = _dun.value ?: return@launch
        if (completed) {
            dunsRepository.completeDun(dun)
            showSnackbarMessage(R.string.dun_marked_complete)
        } else {
            dunsRepository.activateDun(dun)
            showSnackbarMessage(R.string.dun_marked_active)
        }
    }

    fun start(dunId: String?, forceRefresh: Boolean = false) {
        if (_isDataAvailable.value == true && !forceRefresh || _dataLoading.value == true) {
            return
        }

        // Show loading indicator
        _dataLoading.value = true

      wrapEspressoIdlingResource {

        viewModelScope.launch {
          if (dunId != null) {
            dunsRepository.getDun(dunId, false).let { result ->
              if (result is Success) {
                onDunLoaded(result.data)
              } else {
                onDataNotAvailable(result)
              }
            }
          }
          _dataLoading.value = false
        }
      }
    }

    private fun setDun(dun: Dun?) {
        this._dun.value = dun
        _isDataAvailable.value = dun != null
    }

    private fun onDunLoaded(dun: Dun) {
        setDun(dun)
    }

    private fun onDataNotAvailable(result: Result<Dun>) {
        _dun.value = null
        _isDataAvailable.value = false
    }

    fun refresh() {
        dunId?.let { start(it, true) }
    }

    private fun showSnackbarMessage(@StringRes message: Int) {
        _snackbarText.value = Event(message)
    }
}
