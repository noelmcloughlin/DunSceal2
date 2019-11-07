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
import org.noel.dunsceal.util.Event
import org.noel.dunsceal.R
import org.noel.dunsceal.data.Result.Success
import org.noel.dunsceal.data.Dun
import org.noel.dunsceal.data.source.DunsRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for the Add/Edit screen.
 */
class AddEditDunViewModel(
    private val dunsRepository: DunsRepository
) : ViewModel() {

    // Two-way databinding, exposing MutableLiveData
    val title = MutableLiveData<String>()

    // Two-way databinding, exposing MutableLiveData
    val description = MutableLiveData<String>()

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    private val _dunUpdatedEvent = MutableLiveData<Event<Unit>>()
    val dunUpdatedEvent: LiveData<Event<Unit>> = _dunUpdatedEvent

    private var dunId: String? = null

    private var isNewDun: Boolean = false

    private var isDataLoaded = false

    private var dunCompleted = false

    fun start(dunId: String?) {
        if (_dataLoading.value == true) {
            return
        }

        this.dunId = dunId
        if (dunId == null) {
            // No need to populate, it's a new dun
            isNewDun = true
            return
        }
        if (isDataLoaded) {
            // No need to populate, already have data.
            return
        }

        isNewDun = false
        _dataLoading.value = true

        viewModelScope.launch {
            dunsRepository.getDun(dunId).let { result ->
                if (result is Success) {
                    onDunLoaded(result.data)
                } else {
                    onDataNotAvailable()
                }
            }
        }
    }

    private fun onDunLoaded(dun: Dun) {
        title.value = dun.title
        description.value = dun.description
        dunCompleted = dun.isCompleted
        _dataLoading.value = false
        isDataLoaded = true
    }

    private fun onDataNotAvailable() {
        _dataLoading.value = false
    }

    // Called when clicking on fab.
    fun saveDun() {
        val currentTitle = title.value
        val currentDescription = description.value

        if (currentTitle == null || currentDescription == null) {
            _snackbarText.value = Event(R.string.empty_dun_message)
            return
        }
        if (Dun(currentTitle, currentDescription).isEmpty) {
            _snackbarText.value = Event(R.string.empty_dun_message)
            return
        }

        val currentDunId = dunId
        if (isNewDun || currentDunId == null) {
            createDun(Dun(currentTitle, currentDescription))
        } else {
            val dun = Dun(currentTitle, currentDescription, dunCompleted, currentDunId)
            updateDun(dun)
        }
    }

    private fun createDun(newDun: Dun) = viewModelScope.launch {
        dunsRepository.saveDun(newDun)
        _dunUpdatedEvent.value = Event(Unit)
    }

    private fun updateDun(dun: Dun) {
        if (isNewDun) {
            throw RuntimeException("updateDun() was called but dun is new.")
        }
        viewModelScope.launch {
            dunsRepository.saveDun(dun)
            _dunUpdatedEvent.value = Event(Unit)
        }
    }
}