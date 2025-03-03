package com.idz.teamup.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.idz.teamup.R
import com.idz.teamup.model.Group
import com.squareup.picasso.Picasso

class GroupAdapter(private var groups: List<Group>, private val onItemClick: (Group) -> Unit) :
    RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val groupName: TextView = view.findViewById(R.id.groupName)
        val groupActivity: TextView = view.findViewById(R.id.groupActivity)
        val groupDate: TextView = view.findViewById(R.id.groupDate)
        val creatorName: TextView = view.findViewById(R.id.creatorName)
        val groupAdapterImageView:ImageView = view.findViewById(R.id.groupAdapterImageView)
        val groupLocation: TextView = view.findViewById(R.id.groupLocation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.groupName.text = group.name
        holder.groupActivity.text = group.activityType
        holder.groupDate.text = "Date: ${group.dateTime}"
        holder.creatorName.text = "Created by: ${group.createdBy}"
        holder.groupLocation.text = "Location: ${group.location}"

        if (group.imageUrl.isNotEmpty())
            Picasso.get().load(group.imageUrl).into(holder.groupAdapterImageView)
        else
            holder.groupAdapterImageView.setImageDrawable(null)

        holder.itemView.setOnClickListener {
            onItemClick(group)
        }
    }

    override fun getItemCount() = groups.size

    fun updateGroups(newGroups: List<Group>) {
        groups = newGroups
        notifyDataSetChanged()
    }
}
