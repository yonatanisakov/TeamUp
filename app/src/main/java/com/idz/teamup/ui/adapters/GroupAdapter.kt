package com.idz.teamup.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.idz.teamup.R
import com.idz.teamup.local.entity.GroupEntity
import com.idz.teamup.service.DateService
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GroupAdapter(
    groups: List<GroupEntity>,
    private val onItemClick: (GroupEntity) -> Unit
) : BaseGroupAdapter<GroupAdapter.GroupViewHolder>(groups) {

    class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val groupName: TextView = view.findViewById(R.id.groupName)
        val groupActivity: TextView = view.findViewById(R.id.groupActivity)
        val groupDate: TextView = view.findViewById(R.id.groupDate)
        val creatorName: TextView = view.findViewById(R.id.creatorName)
        val groupAdapterImageView: ImageView = view.findViewById(R.id.groupAdapterImageView)
        val groupLocation: TextView = view.findViewById(R.id.groupLocation)
        val viewGroupButton: MaterialButton = view.findViewById(R.id.viewGroupButton)
        val groupCapacity: TextView = view.findViewById(R.id.groupCapacity)
        val pastEventBadge: TextView = view.findViewById(R.id.pastEventBadge)
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
        val isEventPast = DateService.isPastEvent(group.dateTime)

        if (group.imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(group.imageUrl)
                .fit()
                .centerCrop()
                .placeholder(R.drawable.default_group_image)
                .into(holder.groupAdapterImageView)
        } else {
            holder.groupAdapterImageView.setImageResource(R.drawable.default_group_image)
        }
        // Add capacity display
        if (group.maxParticipants > 0) {
            val capacityText = "Members: ${group.members.size}/${group.maxParticipants}"
            holder.groupCapacity.text = capacityText

            // Change text color for nearly full groups
            if (group.members.size >= group.maxParticipants * 0.9) {
                holder.groupCapacity.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.error)
                )
            } else {
                holder.groupCapacity.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.dark_gray)
                )
            }
        } else {
            holder.groupCapacity.text = "Members: ${group.members.size} (unlimited)"
        }
        holder.itemView.setOnClickListener {
            onItemClick(group)
        }

        holder.viewGroupButton.setOnClickListener {
            onItemClick(group)
        }

        holder.pastEventBadge.visibility = if (isEventPast) View.VISIBLE else View.GONE

        if (isEventPast) {
            holder.itemView.alpha = 0.7f
        } else {
            holder.itemView.alpha = 1.0f
        }
    }

}