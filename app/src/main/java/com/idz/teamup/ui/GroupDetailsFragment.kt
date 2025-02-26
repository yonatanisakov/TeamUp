package com.idz.teamup.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.idz.teamup.R
import com.idz.teamup.viewmodel.GroupDetailsViewModel

class GroupDetailsFragment : Fragment(R.layout.fragment_group_details) {

    private val args: GroupDetailsFragmentArgs by navArgs()
    private val viewModel: GroupDetailsViewModel by viewModels()

    private lateinit var groupName: TextView
    private lateinit var groupDescription: TextView
    private lateinit var groupActivity: TextView
    private lateinit var groupDateTime: TextView
    private lateinit var joinLeaveButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupName = view.findViewById(R.id.groupNameDetails)
        groupDescription = view.findViewById(R.id.groupDescriptionDetails)
        groupActivity = view.findViewById(R.id.groupActivityDetails)
        groupDateTime = view.findViewById(R.id.groupDateDetails)
        joinLeaveButton = view.findViewById(R.id.joinLeaveButton)

        viewModel.loadGroupDetails(args.groupId)

        viewModel.group.observe(viewLifecycleOwner) { group ->
            if (group != null) {
                groupName.text = group.name
                groupDescription.text = group.description
                groupActivity.text = group.activityType
                groupDateTime.text = group.dateTime

                if (viewModel.isUserMember()) {
                    joinLeaveButton.text = "Leave Group"
                } else {
                    joinLeaveButton.text = "Join Group"
                }
            } else {
                Toast.makeText(requireContext(), "Group not found!", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        joinLeaveButton.setOnClickListener {
            viewModel.toggleGroupMembership { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Membership updated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to update!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
