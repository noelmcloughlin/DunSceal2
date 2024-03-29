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
package org.noel.dunsceal.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.noel.dunsceal.datasource.DunsRepository
import org.noel.dunsceal.viewmodel.AddEditDunViewModel
import org.noel.dunsceal.viewmodel.DunDetailViewModel
import org.noel.dunsceal.viewmodel.DunsViewModel
import org.noel.dunsceal.viewmodel.StatisticsViewModel

/**
 * Factory for all ViewModels.
 */
@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(
    private val dunsRepository: DunsRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(StatisticsViewModel::class.java) ->
                  StatisticsViewModel(dunsRepository)
                isAssignableFrom(DunDetailViewModel::class.java) ->
                  DunDetailViewModel(dunsRepository)
                isAssignableFrom(AddEditDunViewModel::class.java) ->
                  AddEditDunViewModel(dunsRepository)
                isAssignableFrom(DunsViewModel::class.java) ->
                  DunsViewModel(dunsRepository)
                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}
