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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.noel.dunsceal.util.Event
import org.noel.dunsceal.R
import org.noel.dunsceal.util.Result.Success
import org.noel.dunsceal.model.Dun
import org.noel.dunsceal.datasource.DunsDataSource
import org.noel.dunsceal.datasource.DunsRepository
import org.noel.dunsceal.util.wrapEspressoIdlingResource
import kotlinx.coroutines.launch
import org.noel.dunsceal.activity.ADD_EDIT_RESULT_OK
import org.noel.dunsceal.activity.DELETE_RESULT_OK
import org.noel.dunsceal.activity.EDIT_RESULT_OK
import java.util.ArrayList

/**
 * ViewModel for the dun list screen.
 */
class DunsViewModel(
    private val dunsRepository: DunsRepository
) : ViewModel() {

    private val _items = MutableLiveData<List<Dun>>().apply { value = emptyList() }
    val items: LiveData<List<Dun>> = _items

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _currentFilteringLabel = MutableLiveData<Int>()
    val currentFilteringLabel: LiveData<Int> = _currentFilteringLabel

    private val _noDunsLabel = MutableLiveData<Int>()
    val noDunsLabel: LiveData<Int> = _noDunsLabel

    private val _noDunIconRes = MutableLiveData<Int>()
    val noDunIconRes: LiveData<Int> = _noDunIconRes

    private val _dunsAddViewVisible = MutableLiveData<Boolean>()
    val dunsAddViewVisible: LiveData<Boolean> = _dunsAddViewVisible

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    private var _currentFiltering = DunsFilterType.ALL_TASKS

    // Not used at the moment
    private val isDataLoadingError = MutableLiveData<Boolean>()

    private val _openDunEvent = MutableLiveData<Event<String>>()
    val openDunEvent: LiveData<Event<String>> = _openDunEvent

    private val _newDunEvent = MutableLiveData<Event<Unit>>()
    val newDunEvent: LiveData<Event<Unit>> = _newDunEvent

    // This LiveData depends on another so we can use a transformation.
    val empty: LiveData<Boolean> = Transformations.map(_items) {
        it.isEmpty()
    }

    init {
        // Set initial state
        setFiltering(DunsFilterType.ALL_TASKS)
        loadDuns(true)
    }

    /**
     * Sets the current dun filtering type.
     *
     * @param requestType Can be [DunsFilterType.ALL_TASKS],
     * [DunsFilterType.COMPLETED_TASKS], or
     * [DunsFilterType.ACTIVE_TASKS]
     */
    fun setFiltering(requestType: DunsFilterType) {
        _currentFiltering = requestType

        // Depending on the filter type, set the filtering label, icon drawables, etc.
        when (requestType) {
            DunsFilterType.ALL_TASKS -> {
                setFilter(
                    R.string.label_all, R.string.no_duns_all,
                    R.drawable.logo_no_fill, true
                )
            }
            DunsFilterType.ACTIVE_TASKS -> {
                setFilter(
                    R.string.label_active, R.string.no_duns_active,
                    R.drawable.ic_check_circle_96dp, false
                )
            }
            DunsFilterType.COMPLETED_TASKS -> {
                setFilter(
                    R.string.label_completed, R.string.no_duns_completed,
                    R.drawable.ic_verified_user_96dp, false
                )
            }
        }
    }

    private fun setFilter(
        @StringRes filteringLabelString: Int, @StringRes noDunsLabelString: Int,
        @DrawableRes noDunIconDrawable: Int, dunsAddVisible: Boolean
    ) {
        _currentFilteringLabel.value = filteringLabelString
        _noDunsLabel.value = noDunsLabelString
        _noDunIconRes.value = noDunIconDrawable
        _dunsAddViewVisible.value = dunsAddVisible
    }

    fun clearCompletedDuns() {
        viewModelScope.launch {
            dunsRepository.clearCompletedDuns()
            showSnackbarMessage(R.string.completed_duns_cleared)
            // Refresh list to show the new state
            loadDuns(false)
        }
    }

    fun completeDun(dun: Dun, completed: Boolean) = viewModelScope.launch {
        if (completed) {
            dunsRepository.completeDun(dun)
            showSnackbarMessage(R.string.dun_marked_complete)
        } else {
            dunsRepository.activateDun(dun)
            showSnackbarMessage(R.string.dun_marked_active)
        }
        // Refresh list to show the new state
        loadDuns(false)
    }

    /**
     * Called by the Data Binding library and the FAB's click listener.
     */
    fun addNewDun() {
        _newDunEvent.value = Event(Unit)
    }

    /**
     * Called by Data Binding.
     */
    fun openDun(dunId: String) {
        _openDunEvent.value = Event(dunId)
    }

    fun showEditResultMessage(result: Int) {
        when (result) {
            EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_dun_message)
            ADD_EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_dun_message)
            DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_dun_message)
        }
    }

    private fun showSnackbarMessage(message: Int) {
        _snackbarText.value = Event(message)
    }

    /**
     * @param forceUpdate   Pass in true to refresh the data in the [DunsDataSource]
     */
    fun loadDuns(forceUpdate: Boolean) {
        _dataLoading.value = true

      wrapEspressoIdlingResource {

        viewModelScope.launch {
          val dunsResult = dunsRepository.getDuns(forceUpdate)

          if (dunsResult is Success) {
            val duns = dunsResult.data

            val dunsToShow = ArrayList<Dun>()
            // We filter the duns based on the requestType
            for (dun in duns) {
              when (_currentFiltering) {
                DunsFilterType.ALL_TASKS -> dunsToShow.add(dun)
                DunsFilterType.ACTIVE_TASKS -> if (dun.isPlanned) {
                  dunsToShow.add(dun)
                }
                DunsFilterType.COMPLETED_TASKS -> if (dun.isCompleted) {
                  dunsToShow.add(dun)
                }
              }
            }
            isDataLoadingError.value = false
            _items.value = ArrayList(dunsToShow)
          } else {
            isDataLoadingError.value = false
            _items.value = emptyList()
            showSnackbarMessage(R.string.loading_duns_error)
          }

          _dataLoading.value = false
        }
      }
    }

    fun refresh() {
        loadDuns(true)
    }
}
