package com.idz.teamup.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.idz.teamup.R
import com.idz.teamup.ui.adapters.MemberAdapter
import com.idz.teamup.viewmodel.GroupDetailsViewModel
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.button.MaterialButton
import com.idz.teamup.model.Group
import com.squareup.picasso.Picasso

class GroupDetailsFragment : Fragment(R.layout.fragment_group_details) {

    private val args: GroupDetailsFragmentArgs by navArgs()
    private val viewModel: GroupDetailsViewModel by viewModels()


    private lateinit var collapsingToolbar: CollapsingToolbarLayout
    private lateinit var detailsToolbar: Toolbar
    private lateinit var groupDetailsImageView: ImageView
    private lateinit var groupActivityDetails: TextView
    private lateinit var groupCreatorDetails: TextView
    private lateinit var groupDateDetails: TextView
    private lateinit var groupLocationDetails: TextView
    private lateinit var groupWeatherDetails: TextView
    private lateinit var groupDescriptionDetails: TextView
    private lateinit var membersRecyclerView: RecyclerView
    private lateinit var joinLeaveButton: MaterialButton
    private lateinit var editGroupButton: MaterialButton
    private lateinit var deleteGroupButton: MaterialButton
    private lateinit var ownerActionsContainer: LinearLayout
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var memberAdapter: MemberAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI(view)
        observeData()
        setupClickListeners()


        viewModel.loadGroupDetails(args.groupId)


    }



    private fun setupUI(view: View) {
        collapsingToolbar = view.findViewById(R.id.collapsingToolbar)
        detailsToolbar = view.findViewById(R.id.detailsToolbar)
        groupDetailsImageView = view.findViewById(R.id.groupDetailsImageView)
        groupActivityDetails = view.findViewById(R.id.groupActivityDetails)
        groupCreatorDetails = view.findViewById(R.id.groupCreatorDetails)
        groupDateDetails = view.findViewById(R.id.groupDateDetails)
        groupLocationDetails = view.findViewById(R.id.groupLocationDetails)
        groupWeatherDetails = view.findViewById(R.id.groupWeatherDetails)
        groupDescriptionDetails = view.findViewById(R.id.groupDescriptionDetails)
        membersRecyclerView = view.findViewById(R.id.membersRecyclerView)
        joinLeaveButton = view.findViewById(R.id.joinLeaveButton)
        editGroupButton = view.findViewById(R.id.editGroupButton)
        deleteGroupButton = view.findViewById(R.id.deleteGroupButton)
        ownerActionsContainer = view.findViewById(R.id.ownerActionsContainer)
        loadingOverlay = view.findViewById(R.id.loadingOverlay)

        membersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        memberAdapter = MemberAdapter(emptyList())
        membersRecyclerView.adapter = memberAdapter
    }
    private fun observeData() {
        viewModel.group.observe(viewLifecycleOwner) { group  ->
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
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
            joinLeaveButton.isEnabled = !isLoading
            editGroupButton.isEnabled = !isLoading
            deleteGroupButton.isEnabled = !isLoading
        }
    }

        private fun updateGroupUI(group: Group) {
            collapsingToolbar.title = group.name

            if (group.imageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(group.imageUrl)
                    .placeholder(R.drawable.default_group_image)
                    .error(R.drawable.default_group_image)
                    .into(groupDetailsImageView)
            } else {
                groupDetailsImageView.setImageResource(R.drawable.default_group_image)
            }

            groupActivityDetails.text = group.activityType
            groupCreatorDetails.text = "Created by: ${group.createdBy}"
            groupDateDetails.text = group.dateTime
            groupLocationDetails.text = group.location
            groupDescriptionDetails.text = group.description

            joinLeaveButton.text = if (viewModel.isUserMember()) "Leave Group" else "Join Group"

            ownerActionsContainer.visibility = if (viewModel.isUserCreator()) View.VISIBLE else View.GONE
    }

    private fun setupClickListeners() {
        detailsToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        joinLeaveButton.setOnClickListener {

            viewModel.toggleGroupMembership { success ->
                requireActivity().runOnUiThread {

                    if (!success) {
                        Toast.makeText(requireContext(), "Failed to update membership", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        editGroupButton.setOnClickListener {
            val action = GroupDetailsFragmentDirections.actionGroupDetailsToEditGroup(groupId = args.groupId)
            findNavController().navigate(action)
        }

        deleteGroupButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }
    private fun showDeleteConfirmationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_delete, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.confirmDeleteButton).setOnClickListener {
            dialog.dismiss()
            deleteGroup()
        }

        dialog.show()
    }
    private fun deleteGroup() {

        viewModel.deleteGroup(args.groupId) { success ->
            try {
                if (isAdded && activity != null) {
                    requireActivity().runOnUiThread {
                        loadingOverlay.visibility = View.GONE

                        if (success) {
                            Toast.makeText(requireContext(), "Group deleted!", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        } else {
                            Toast.makeText(requireContext(), "Delete failed!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (Exception: IllegalStateException) {
                Log.e("GroupDetailsFragment", "Fragment not attached when completing delete operation")
            }
        }
    }
    override fun onDestroyView() {
        membersRecyclerView.adapter = null

        super.onDestroyView()
    }
}

