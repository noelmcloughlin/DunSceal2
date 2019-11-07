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
package org.noel.dunsceal.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.noel.dunsceal.util.EventObserver
import org.noel.dunsceal.R
import org.noel.dunsceal.databinding.AdddunFragBinding
import org.noel.dunsceal.activity.ADD_EDIT_RESULT_OK
import org.noel.dunsceal.util.setupRefreshLayout
import org.noel.dunsceal.util.setupSnackbar
import com.google.android.material.snackbar.Snackbar
import org.noel.dunsceal.viewmodel.AddEditDunViewModel

/**
 * Main UI for the add dun screen. Users can enter a dun title and description.
 */
class AddEditDunFragment : Fragment() {

    private lateinit var viewDataBinding: AdddunFragBinding

    private val args: AddEditDunFragmentArgs by navArgs()

    private val viewModel by viewModels<AddEditDunViewModel> { getViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.adddun_frag, container, false)
        viewDataBinding = AdddunFragBinding.bind(root).apply {
            this.viewmodel = viewModel
        }
        // Set the lifecycle owner to the lifecycle of the view
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupSnackbar()
        setupNavigation()
        this.setupRefreshLayout(viewDataBinding.refreshLayout)
        viewModel.start(args.dunId)
    }

    private fun setupSnackbar() {
        view?.setupSnackbar(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
    }

    private fun setupNavigation() {
        viewModel.dunUpdatedEvent.observe(this, EventObserver {
            val action = AddEditDunFragmentDirections.actionAddEditDunFragmentToDunsFragment(ADD_EDIT_RESULT_OK)
            findNavController().navigate(action)
        })
    }
}
