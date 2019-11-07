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
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.noel.dunsceal.util.EventObserver
import org.noel.dunsceal.R
import org.noel.dunsceal.data.Dun
import org.noel.dunsceal.databinding.DunsFragBinding
import org.noel.dunsceal.util.setupRefreshLayout
import org.noel.dunsceal.util.setupSnackbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import org.noel.dunsceal.adapter.DunsAdapter
import org.noel.dunsceal.viewmodel.DunsFilterType
import org.noel.dunsceal.viewmodel.DunsViewModel
import timber.log.Timber

/**
 * Display a grid of [Dun]s. User can choose to view all, active or completed duns.
 */
class DunsFragment : Fragment() {

    private val viewModel by viewModels<DunsViewModel> { getViewModelFactory() }
    private val args: DunsFragmentArgs by navArgs()
    private lateinit var viewDataBinding: DunsFragBinding
    private lateinit var listAdapter: DunsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = DunsFragBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }
        setHasOptionsMenu(true)
        return viewDataBinding.root
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.menu_clear -> {
                viewModel.clearCompletedDuns()
                true
            }
            R.id.menu_filter -> {
                showFilteringPopUpMenu()
                true
            }
            R.id.menu_refresh -> {
                viewModel.loadDuns(true)
                true
            }
            else -> false
        }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.duns_fragment_menu, menu)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Set the lifecycle owner to the lifecycle of the view
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        setupSnackbar()
        setupListAdapter()
        setupRefreshLayout(viewDataBinding.refreshLayout, viewDataBinding.dunsList)
        setupNavigation()
        setupFab()

        // Always reloading data for simplicity. Real apps should only do this on first load and
        // when navigating back to this destination. TODO: https://issuetracker.google.com/79672220
        viewModel.loadDuns(true)
    }

    private fun setupNavigation() {
        viewModel.openDunEvent.observe(this, EventObserver {
            openDunDetails(it)
        })
        viewModel.newDunEvent.observe(this, EventObserver {
            navigateToAddNewDun()
        })
    }

    private fun setupSnackbar() {
        view?.setupSnackbar(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
        arguments?.let {
            viewModel.showEditResultMessage(args.userMessage)
        }
    }

    private fun showFilteringPopUpMenu() {
        val view = activity?.findViewById<View>(R.id.menu_filter) ?: return
        PopupMenu(requireContext(), view).run {
            menuInflater.inflate(R.menu.filter_duns, menu)

            setOnMenuItemClickListener {
                viewModel.setFiltering(
                    when (it.itemId) {
                        R.id.active -> DunsFilterType.ACTIVE_TASKS
                        R.id.completed -> DunsFilterType.COMPLETED_TASKS
                        else -> DunsFilterType.ALL_TASKS
                    }
                )
                viewModel.loadDuns(false)
                true
            }
            show()
        }
    }

    private fun setupFab() {
        activity?.findViewById<FloatingActionButton>(R.id.add_dun_fab)?.let {
            it.setOnClickListener {
                navigateToAddNewDun()
            }
        }
    }

    private fun navigateToAddNewDun() {
        val action = DunsFragmentDirections.actionDunsFragmentToAddEditDunFragment(
            null,
            resources.getString(R.string.add_dun)
        )
        findNavController().navigate(action)
    }

    private fun openDunDetails(dunId: String) {
        val action = DunsFragmentDirections.actionDunsFragmentToDunDetailFragment(dunId)
        findNavController().navigate(action)
    }

    private fun setupListAdapter() {
        val viewModel = viewDataBinding.viewmodel
        if (viewModel != null) {
            listAdapter = DunsAdapter(viewModel)
            viewDataBinding.dunsList.adapter = listAdapter
        } else {
            Timber.w("ViewModel not initialized when attempting to set up adapter.")
        }
    }
}
