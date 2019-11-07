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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.noel.dunsceal.util.EventObserver
import org.noel.dunsceal.R
import org.noel.dunsceal.activity.DELETE_RESULT_OK
import org.noel.dunsceal.util.setupRefreshLayout
import org.noel.dunsceal.util.setupSnackbar
import com.google.android.material.snackbar.Snackbar
import org.noel.dunsceal.databinding.DundetailFragBinding
import org.noel.dunsceal.viewmodel.DunDetailViewModel


/**
 * Main UI for the dun detail screen.
 */
class DunDetailFragment : Fragment() {
    private lateinit var viewDataBinding: DundetailFragBinding

    private val args: DunDetailFragmentArgs by navArgs()

    private val viewModel by viewModels<DunDetailViewModel> { getViewModelFactory() }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupFab()
        view?.setupSnackbar(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
        setupNavigation()
        this.setupRefreshLayout(viewDataBinding.refreshLayout)
    }

    private fun setupNavigation() {
        viewModel.deleteDunEvent.observe(this, EventObserver {
            val action = DunDetailFragmentDirections.actionDunDetailFragmentToDunsFragment(DELETE_RESULT_OK)
            findNavController().navigate(action)
        })
        viewModel.editDunEvent.observe(this, EventObserver {
            val action = DunDetailFragmentDirections.actionDunDetailFragmentToAddEditDunFragment(
                args.dunId,
                resources.getString(R.string.edit_dun)
            )
            findNavController().navigate(action)
        })
    }

    private fun setupFab() {
        activity?.findViewById<View>(R.id.edit_dun_fab)?.setOnClickListener {
            viewModel.editDun()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dundetail_frag, container, false)
        viewDataBinding = DundetailFragBinding.bind(view).apply {
            viewmodel = viewModel
        }
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner

        viewModel.start(args.dunId)

        setHasOptionsMenu(true)
        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete -> {
                viewModel.deleteDun()
                true
            }
            else -> false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dundetail_fragment_menu, menu)
    }
}
