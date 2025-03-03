package com.idz.teamup.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.idz.teamup.R
import com.idz.teamup.ui.adapters.MemberAdapter
import com.idz.teamup.viewmodel.GroupDetailsViewModel
import androidx.navigation.fragment.findNavController
import com.idz.teamup.model.Group
import com.squareup.picasso.Picasso

class GroupDetailsFragment : Fragment(R.layout.fragment_group_details) {

    private val args: GroupDetailsFragmentArgs by navArgs()
    private val viewModel: GroupDetailsViewModel by viewModels()

    private lateinit var groupCreator: TextView
    private lateinit var groupName: TextView
    private lateinit var groupDescription: TextView
    private lateinit var groupActivity: TextView
    private lateinit var groupDateTime: TextView
    private lateinit var joinLeaveButton: Button
    private lateinit var membersRecyclerView: RecyclerView
    private lateinit var memberAdapter: MemberAdapter
    private lateinit var deleteGroupButton: Button
    private lateinit var editGroupButton: Button
    private lateinit var groupDetailsImageView: ImageView
    private lateinit var groupLocation: TextView
    private lateinit var groupWeatherDetails: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI(view)
        viewModel.loadGroupDetails(args.groupId)

        viewModel.group.observe(viewLifecycleOwner) { group ->
            group?.let {
                updateGroupUI(it)

                if (viewModel.weather.value.isNullOrEmpty()) {
                    viewModel.loadWeather(it.location, it.dateTime)
                }
            } ?: run {
                Toast.makeText(requireContext(), "Group not found!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        viewModel.weather.observe(viewLifecycleOwner) { weather ->
            if (groupWeatherDetails.text != weather) {
                groupWeatherDetails.text = weather
            }
        }

        viewModel.members.observe(viewLifecycleOwner) { members ->
            memberAdapter.updateMembers(members)
        }
        setupClickListeners()
    }



    private fun setupUI(view: View) {
        deleteGroupButton = view.findViewById(R.id.deleteGroupButton)
        editGroupButton = view.findViewById(R.id.editGroupButton)
        membersRecyclerView = view.findViewById(R.id.membersRecyclerView)
        joinLeaveButton = view.findViewById(R.id.joinLeaveButton)
        groupCreator = view.findViewById(R.id.groupCreatorDetails)
        groupName = view.findViewById(R.id.groupNameDetails)
        groupDescription = view.findViewById(R.id.groupDescriptionDetails)
        groupActivity = view.findViewById(R.id.groupActivityDetails)
        groupDateTime = view.findViewById(R.id.groupDateDetails)
        groupLocation = view.findViewById(R.id.groupLocationDetails)
        groupWeatherDetails = view.findViewById(R.id.groupWeatherDetails)
        groupDetailsImageView = view.findViewById(R.id.groupDetailsImageView)

        membersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        memberAdapter = MemberAdapter(emptyList())
        membersRecyclerView.adapter = memberAdapter
    }
    private fun updateGroupUI(group: Group) {
        groupName.text = group.name
        groupDescription.text = group.description
        groupActivity.text = group.activityType
        groupDateTime.text = group.dateTime
        groupCreator.text = "Created by: ${group.createdBy}"
        groupLocation.text = "Location: ${group.location}"
        joinLeaveButton.text = if (viewModel.isUserMember()) "Leave Group" else "Join Group"

        if (viewModel.isUserCreator()) {
            deleteGroupButton.visibility = View.VISIBLE
            editGroupButton.visibility = View.VISIBLE
        } else {
            deleteGroupButton.visibility = View.GONE
            editGroupButton.visibility = View.GONE
        }

        if (group.imageUrl.isNotEmpty()) {
            Picasso.get().load(group.imageUrl).into(groupDetailsImageView)
        }
    }
    private fun setupClickListeners() {
        joinLeaveButton.setOnClickListener {
            joinLeaveButton.isEnabled = false
            viewModel.toggleGroupMembership { success ->
                joinLeaveButton.isEnabled = true
                if (!success)
                    Toast.makeText(requireContext(), "Failed to update membership", Toast.LENGTH_SHORT).show()
            }
        }

        editGroupButton.setOnClickListener {
            val action =
                GroupDetailsFragmentDirections.actionGroupDetailsToEditGroup(groupId = args.groupId)
            findNavController().navigate(action)
        }

        deleteGroupButton.setOnClickListener {
            viewModel.deleteGroup(args.groupId) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Group deleted!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), "Delete failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

