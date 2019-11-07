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
package org.noel.dunsceal.adapter

    import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.noel.dunsceal.data.Dun
    import org.noel.dunsceal.databinding.DunItemBinding
    import org.noel.dunsceal.viewmodel.DunsViewModel

/**
 * Adapter for the dun list. Has a reference to the [DunsViewModel] to send actions back to it.
 */
class DunsAdapter(private val viewModel: DunsViewModel) :
    ListAdapter<Dun, DunsAdapter.ViewHolder>(DunDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(viewModel, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: DunItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: DunsViewModel, item: Dun) {

            binding.viewmodel = viewModel
            binding.dun = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = DunItemBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

/**
 * Callback for calculating the diff between two non-null items in a list.
 *
 * Used by ListAdapter to calculate the minimum number of changes between and old list and a new
 * list that's been passed to `submitList`.
 */
class DunDiffCallback : DiffUtil.ItemCallback<Dun>() {
    override fun areItemsTheSame(oldItem: Dun, newItem: Dun): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Dun, newItem: Dun): Boolean {
        return oldItem == newItem
    }
}
