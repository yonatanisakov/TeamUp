package com.idz.teamup.ui.adapters

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.idz.teamup.local.entity.GroupEntity

abstract class BaseGroupAdapter<T : RecyclerView.ViewHolder>(
    protected var groups: List<GroupEntity>
) : RecyclerView.Adapter<T>() {

    override fun getItemCount() = groups.size

    fun updateGroups(newGroups: List<GroupEntity>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = groups.size
            override fun getNewListSize(): Int = newGroups.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return groups[oldItemPosition].groupId == newGroups[newItemPosition].groupId
            }
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return groups[oldItemPosition] == newGroups[newItemPosition]
            }
        }

        val diffResult = DiffUtil.calculateDiff(diffCallback)
        groups = newGroups
        diffResult.dispatchUpdatesTo(this)
    }
}