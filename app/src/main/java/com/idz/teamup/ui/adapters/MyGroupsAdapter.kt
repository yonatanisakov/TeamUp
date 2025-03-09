package com.idz.teamup.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.idz.teamup.R
import com.idz.teamup.local.entity.GroupEntity
import com.squareup.picasso.Picasso

class MyGroupsAdapter(
    groups: List<GroupEntity>,
    private val onItemClick: (GroupEntity) -> Unit,
    private val onManageClick: (GroupEntity) -> Unit
) : BaseGroupAdapter<MyGroupsAdapter.MyGroupViewHolder>(groups) {

    class MyGroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val groupImageView: ImageView = view.findViewById(R.id.groupImageView)
        val groupName: TextView = view.findViewById(R.id.groupName)
        val groupActivity: TextView = view.findViewById(R.id.groupActivity)
        val memberCount: TextView = view.findViewById(R.id.memberCount)
        val manageButton: MaterialButton = view.findViewById(R.id.manageButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyGroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_group, parent, false)
        return MyGroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyGroupViewHolder, position: Int) {
        val group = groups[position]

        holder.groupName.text = group.name
        holder.groupActivity.text = group.activityType

        val memberText = if (group.members.size == 1) {
            "1 member"
        } else {
            "${group.members.size} members"
        }
        holder.memberCount.text = memberText

        if (group.imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(group.imageUrl)
                .fit()
                .centerCrop()
                .placeholder(R.drawable.default_group_image)
                .into(holder.groupImageView)
        } else {
            holder.groupImageView.setImageResource(R.drawable.default_group_image)
        }

        holder.itemView.setOnClickListener {
            onItemClick(group)
        }

        holder.manageButton.setOnClickListener {
            onManageClick(group)
        }
    }
}